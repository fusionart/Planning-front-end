package com.monbat.pages.readinessComponent;

import com.monbat.models.dto.ReadinessDetail;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.*;
import java.util.stream.Collectors;

public class ReadinessTable extends Panel {
    private final List<ReadinessDetail> allData;
    private List<ReadinessDetail> filteredData;
    private final Set<ReadinessDetail> selectedRows = new HashSet<>();
    private String filterText = "";
    private String filterColumn = "all";
    private DataTable<ReadinessDetail, String> dataTable;
    private IModel<Integer> rowsPerPage = Model.of(10);

    public ReadinessTable(String id, IModel<Collection<ReadinessDetail>> model) {
        super(id);
        setOutputMarkupId(true);
        this.allData = new ArrayList<>(model.getObject());
        this.filteredData = new ArrayList<>(allData);

        // Create the main form
        Form<?> form = new Form<>("form");
        add(form);

        // Add filter controls
        addFilterControls(form);

        // Add the data table
        dataTable = createDataTable("table");
        dataTable.setOutputMarkupId(true);
        //form.add(dataTable);

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
        List<String> columns = Arrays.asList("all", "productionPlant", "salesDocument", "customerName",
                "batteryType", "material", "workCenter");
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

    private DataTable<ReadinessDetail, String> createDataTable(String id) {
        List<IColumn<ReadinessDetail, String>> columns = new ArrayList<>();

        // Checkbox column for row selection
        columns.add(new AbstractColumn<>(Model.of("")) {
            @Override
            public void populateItem(Item<ICellPopulator<ReadinessDetail>> cellItem,
                                     String componentId, IModel<ReadinessDetail> rowModel) {
                cellItem.add(new CheckboxPanel(componentId, rowModel, selectedRows));
            }
        });

        // Data columns
        columns.add(createColumn("Production Plant", "productionPlant"));
        columns.add(createColumn("Sales Document", "salesDocument"));
        columns.add(createColumn("Sold To Party", "soldToParty"));
        columns.add(createColumn("Customer Name", "customerName"));
        columns.add(createColumn("Req Dlv Week", "reqDlvWeek"));
        columns.add(createColumn("Battery Type", "batteryType"));
        columns.add(createColumn("Material", "material"));
        columns.add(createColumn("Order Quantity", "orderQuantity"));
        columns.add(createColumn("Work Center", "workCenter"));

        ReadinessDataProvider dataProvider = new ReadinessDataProvider();

        return new DefaultDataTable<>(id, columns, dataProvider, rowsPerPage.getObject());
    }

    private PropertyColumn<ReadinessDetail, String> createColumn(String header, String propertyExpression) {
        return new PropertyColumn<>(Model.of(header), propertyExpression, propertyExpression) {
            @Override
            public void populateItem(Item<ICellPopulator<ReadinessDetail>> item, String componentId, IModel<ReadinessDetail> rowModel) {
                ReadinessDetail detail = rowModel.getObject();
//                System.out.println(header + ": " + detail); // Debugging output
                super.populateItem(item, componentId, rowModel);
            }
        };
    }


    private void filterData() {
        if (filterText == null || filterText.isEmpty()) {
            filteredData = new ArrayList<>(allData);
            return;
        }

        filteredData = allData.stream()
                .filter(detail -> matchesFilter(detail, filterText.toLowerCase()))
                .collect(Collectors.toList());
    }

    private boolean matchesFilter(ReadinessDetail detail, String filter) {
        if ("all".equals(filterColumn)) {
            return detail.getProductionPlant() == Integer.parseInt(filter) ||
                    detail.getSalesDocument() == Integer.parseInt(filter) ||
                    detail.getCustomerName().toLowerCase().contains(filter) ||
                    detail.getBatteryType().toLowerCase().contains(filter) ||
                    detail.getMaterial().toLowerCase().contains(filter) ||
                    detail.getWorkCenter().toLowerCase().contains(filter);
        }

        // Filter by specific column
        return switch (filterColumn) {
            case "productionPlant" -> detail.getProductionPlant() == Integer.parseInt(filter);
            case "salesDocument" -> detail.getSalesDocument() == Integer.parseInt(filter);
            case "customerName" -> detail.getCustomerName().toLowerCase().contains(filter);
            case "batteryType" -> detail.getBatteryType().toLowerCase().contains(filter);
            case "material" -> detail.getMaterial().toLowerCase().contains(filter);
            case "workCenter" -> detail.getWorkCenter().toLowerCase().contains(filter);
            default -> false;
        };
    }

    private void exportToCSV() {
        StringBuilder csv = new StringBuilder();
        // Add headers
        csv.append("Production Plant,Sales Document,Sold To Party,Customer Name,Req Dlv Week," +
                "Battery Type,Material,Order Quantity,Work Center\n");

        // Add data rows
        for (ReadinessDetail detail : selectedRows.isEmpty() ? filteredData : selectedRows) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    detail.getProductionPlant(),
                    detail.getSalesDocument(),
                    detail.getSoldToParty(),
                    detail.getCustomerName(),
                    detail.getReqDlvWeek(),
                    detail.getBatteryType(),
                    detail.getMaterial(),
                    detail.getOrderQuantity(),
                    detail.getWorkCenter()));
        }

        // Create resource for download
        CSVResourceDownload resource = new CSVResourceDownload(
                csv.toString().getBytes(),
                "export.csv"
        );

        getRequestCycle().scheduleRequestHandlerAfterCurrent(
                new ResourceRequestHandler(resource, new PageParameters())
        );
    }

    private class ReadinessDataProvider extends SortableDataProvider<ReadinessDetail, String> {
        private String sortProperty = "productionPlant";
        private SortOrder sortOrder = SortOrder.ASCENDING;

        public ReadinessDataProvider() {
            setSort(sortProperty, sortOrder);
        }

        @Override
        public Iterator<? extends ReadinessDetail> iterator(long first, long count) {
            List<ReadinessDetail> sorted = new ArrayList<>(filteredData);
            sorted.sort((o1, o2) -> {
                int compare = compareByProperty(o1, o2, getSort().getProperty());
                return getSort().isAscending() ? compare : -compare;
            });

            return sorted.subList((int)first, (int)Math.min(first + count, sorted.size())).iterator();
        }

        @Override
        public long size() {
            return filteredData.size();
        }

        @Override
        public IModel<ReadinessDetail> model(ReadinessDetail object) {
            return Model.of(object);
        }

        private int compareByProperty(ReadinessDetail o1, ReadinessDetail o2, String property) {
            return switch (property) {
                case "productionPlant" -> o1.getProductionPlant().compareTo(o2.getProductionPlant());
                case "salesDocument" -> o1.getSalesDocument().compareTo(o2.getSalesDocument());
                case "customerName" -> o1.getCustomerName().compareTo(o2.getCustomerName());
                // Add other properties as needed
                default -> 0;
            };
        }
    }
}