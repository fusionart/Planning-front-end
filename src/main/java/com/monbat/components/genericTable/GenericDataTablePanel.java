package com.monbat.components.genericTable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenericDataTablePanel<T extends Serializable> extends Panel {
    private final List<T> allData;
    private List<T> filteredData;
    private final Set<T> selectedRows = new HashSet<>();
    private String filterText = "";
    private String filterColumn = "all";
    private final DataTable<T, String> dataTable;
    private final IModel<Integer> rowsPerPage = Model.of(10);
    private final List<ColumnDefinition<T>> columnDefinitions;
    private final Function<T, List<String>> filterFunction;

    public GenericDataTablePanel(String id,
                                 IModel<Collection<T>> model,
                                 List<ColumnDefinition<T>> columnDefinitions,
                                 Function<T, List<String>> filterFunction) {
        super(id);
        setOutputMarkupId(true);
        this.allData = new ArrayList<>(model.getObject());
        this.filteredData = new ArrayList<>(allData);
        this.columnDefinitions = columnDefinitions;
        this.filterFunction = filterFunction;

        // Create the main form
        Form<?> form = new Form<>("form");
        add(form);

        // Add filter controls
        addFilterControls(form);

        // Create list of filterable columns
        List<String> filterableColumns = columnDefinitions.stream()
                .map(ColumnDefinition::getHeader)
                .collect(Collectors.toList());
        filterableColumns.add(0, "all");

        // Add the data table
        dataTable = createDataTable("table", filterableColumns);
        dataTable.setOutputMarkupId(true);

        WebMarkupContainer tableContainer = new WebMarkupContainer("tableContainer");
        tableContainer.setOutputMarkupId(true);
        tableContainer.add(dataTable);
        form.add(tableContainer);

        // Add export button
        addExportButton(form);

        // Add rows per page selector
        addRowsPerPageSelector(form);
    }

    private void addFilterControls(Form<?> form) {
        // Text field for filtering
        TextField<String> filterField = new TextField<>("filterField", new PropertyModel<>(this, "filterText"));
        filterField.add(new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filterData();
                target.add(dataTable);
            }
        });
        form.add(filterField);

        // Dropdown for selecting filter column
        List<String> columns = columnDefinitions.stream()
                .map(ColumnDefinition::getHeader)
                .collect(Collectors.toList());
        columns.add(0, "all");
        DropDownChoice<String> columnSelector = new DropDownChoice<>("filterColumn",
                new PropertyModel<>(this, "filterColumn"), columns);
        columnSelector.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filterData();
                target.add(dataTable);
            }
        });
        form.add(columnSelector);
    }

    private void addExportButton(Form<?> form) {
        AjaxButton exportButton = new AjaxButton("exportButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                exportToCSV();
            }
        };
        form.add(exportButton);
    }

    private void addRowsPerPageSelector(Form<?> form) {
        List<Integer> pageSizes = Arrays.asList(10, 25, 50, 100);
        DropDownChoice<Integer> rowsPerPageChoice = new DropDownChoice<>("rowsPerPage",
                rowsPerPage, pageSizes);
        rowsPerPageChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                dataTable.setItemsPerPage(rowsPerPage.getObject());
                target.add(dataTable);
            }
        });
        form.add(rowsPerPageChoice);
    }

    private DataTable<T, String> createDataTable(String id, List<String> filterableColumns) {
        List<IColumn<T, String>> columns = new ArrayList<>();

        // Add columns from definitions
        for (ColumnDefinition<T> definition : columnDefinitions) {
            columns.add(definition.createColumn());
        }

        GenericDataProvider dataProvider = new GenericDataProvider();

        // Create the data table with footer
        DataTable<T, String> table = new DefaultDataTable<>(id, columns, dataProvider, rowsPerPage.getObject());
        table.addBottomToolbar(new GenericAggregateToolbar<>(table, dataProvider));

        return table;
    }

    private void filterData() {
        if (filterText == null || filterText.isEmpty()) {
            filteredData = new ArrayList<>(allData);
        } else {
            filteredData = allData.stream()
                    .filter(this::matchesFilter)
                    .collect(Collectors.toList());
        }
    }

    private boolean matchesFilter(T item) {
        List<String> searchableValues = filterFunction.apply(item);
        String lowercaseFilter = filterText.toLowerCase();

        // If no filter column specified or 'all' is selected
        if ("all".equals(filterColumn)) {
            return searchableValues.stream()
                    .anyMatch(value -> value.toLowerCase().contains(lowercaseFilter));
        }

        // Filter by specific column
        int index = columnDefinitions.stream()
                .map(ColumnDefinition::getHeader)
                .toList()
                .indexOf(filterColumn);

        if (index >= 0 && index < searchableValues.size()) {
            return searchableValues.get(index).toLowerCase().contains(lowercaseFilter);
        }

        return false;
    }

    private void exportToCSV() {
        // Implement CSV export logic here
        // You might want to pass in a custom export function similar to filterFunction
    }

    private class GenericDataProvider extends SortableDataProvider<T, String> {
        @Override
        public Iterator<? extends T> iterator(long first, long count) {
            List<T> data = new ArrayList<>(filteredData);

            // Apply sorting if specified
            if (getSort() != null) {
                String sortProperty = getSort().getProperty();
                boolean ascending = getSort().isAscending();

                data.sort((o1, o2) -> {
                    try {
                        Object val1 = PropertyResolver.getValue(sortProperty, o1);
                        Object val2 = PropertyResolver.getValue(sortProperty, o2);

                        if (val1 == null && val2 == null) return 0;
                        if (val1 == null) return ascending ? -1 : 1;
                        if (val2 == null) return ascending ? 1 : -1;

                        if (val1 instanceof Comparable && val2 instanceof Comparable) {
                            int compareResult = ((Comparable) val1).compareTo(val2);
                            return ascending ? compareResult : -compareResult;
                        }

                        return String.valueOf(val1).compareTo(String.valueOf(val2));
                    } catch (Exception e) {
                        return 0;
                    }
                });
            }

            return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
        }

        @Override
        public long size() {
            return filteredData.size();
        }

        @Override
        public IModel<T> model(T object) {
            return Model.of(object);
        }
    }
}