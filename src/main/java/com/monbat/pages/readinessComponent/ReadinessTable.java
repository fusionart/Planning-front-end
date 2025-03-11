package com.monbat.pages.readinessComponent;

import com.monbat.models.dto.ReadinessDetail;
import com.monbat.models.dto.ReadinessDetailWithDate;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.*;
import java.util.stream.Collectors;

public class ReadinessTable extends Panel {
    private final List<ReadinessDetailWithDate> allData;
    private List<ReadinessDetailWithDate> filteredData;
    private final Set<ReadinessDetailWithDate> selectedRows = new HashSet<>();
    private String filterText = "";
    private String filterColumn = "all";
    private String filterDate = "all";
    private final DataTable<ReadinessDetailWithDate, String> dataTable;
    private final IModel<Integer> rowsPerPage = Model.of(10);

    public ReadinessTable(String id, IModel<Collection<ReadinessDetailWithDate>> model) {
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
        List<String> columns = Arrays.asList("all", "productionDate", "productionPlant", "salesDocument",
                "customerName", "batteryType", "material", "workCenter");
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

        // Dropdown for selecting filter date
        List<String> dates = allData.stream()
                .map(ReadinessDetailWithDate::getDate)
                .distinct()
                .map(Date::toString)
                .collect(Collectors.toList());
        dates.add(0, "all");
        DropDownChoice<String> dateSelector = new DropDownChoice<>("filterDate",
                new PropertyModel<>(this, "filterDate"), dates);
        dateSelector.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filterData();
                target.add(dataTable);
            }
        });
        form.add(dateSelector);
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

    private DataTable<ReadinessDetailWithDate, String> createDataTable(String id) {
        List<IColumn<ReadinessDetailWithDate, String>> columns = new ArrayList<>();

        // Checkbox column
        columns.add(new AggregatingPropertyColumn<>(Model.of(""), "", true, true) {
            @Override
            public void populateItem(Item<ICellPopulator<ReadinessDetailWithDate>> cellItem, String componentId, IModel<ReadinessDetailWithDate> rowModel) {
                cellItem.add(new CheckboxPanel(componentId, rowModel, selectedRows));
            }

            @Override
            public void populateFooterCell(Item<ICellPopulator<ReadinessDetailWithDate>> item, String componentId, IDataProvider<ReadinessDetailWithDate> dataProvider) {
                item.add(new Label(componentId, ""));
            }

            @Override
            public Object getData(ReadinessDetailWithDate object) {
                return "";
            }
        });

        // Date column
        columns.add(createColumn("Date", "date"));

        // Data columns with aggregation
        columns.add(createColumnWithCount("Production Plant", "detail.productionPlant"));
        columns.add(createColumn("Sales Document", "detail.salesDocument"));
        columns.add(createColumn("Sold To Party", "detail.soldToParty"));
        columns.add(createColumn("Customer Name", "detail.customerName"));
        columns.add(createColumn("Req Dlv Week", "detail.reqDlvWeek"));
        columns.add(createColumn("Battery Type", "detail.batteryType"));
        columns.add(createColumn("Material", "detail.material"));
        columns.add(createColumnWithCountAndSum("Order Quantity", "detail.orderQuantity"));
        columns.add(createColumnWithCount("Work Center", "detail.workCenter"));

        ReadinessDataProvider dataProvider = new ReadinessDataProvider();

        // Create the data table with footer
        DataTable<ReadinessDetailWithDate, String> table = new DefaultDataTable<>(id, columns, dataProvider, rowsPerPage.getObject());
        table.addBottomToolbar(new AggregateToolbar<>(table, dataProvider));

        return table;
    }

    private PropertyColumn<ReadinessDetailWithDate, String> createColumn(String header, String propertyExpression) {
        return new PropertyColumn<>(Model.of(header), propertyExpression, propertyExpression);
    }

    private AggregatingPropertyColumn<ReadinessDetailWithDate, String> createColumnWithCount(String header, String propertyExpression) {
        return new AggregatingPropertyColumn<>(Model.of(header), propertyExpression, false, true) {
            @Override
            public void populateFooterCell(Item<ICellPopulator<ReadinessDetailWithDate>> item, String componentId,
                                           IDataProvider<ReadinessDetailWithDate> dataProvider) {
                filterData();
                long uniqueCount = filteredData.stream().map(d -> getObjectAsString(d, propertyExpression)).distinct().count();
                item.add(new Label(componentId, "Count: " + uniqueCount));
            }

            @Override
            public Object getData(ReadinessDetailWithDate object) {
                return getObjectAsString(object, propertyExpression);
            }
        };
    }

    private AggregatingPropertyColumn<ReadinessDetailWithDate, String> createColumnWithSum(String header, String propertyExpression) {
        return new AggregatingPropertyColumn<>(Model.of(header), propertyExpression, true, false) {
            @Override
            public void populateFooterCell(Item<ICellPopulator<ReadinessDetailWithDate>> item, String componentId,
                                           IDataProvider<ReadinessDetailWithDate> dataProvider) {
                // Use the filtered data directly
                filterData();
                List<ReadinessDetailWithDate> data = filteredData;

                double sum = data.stream()
                        .mapToDouble(d -> {
                            Object value = getObjectValue(d, propertyExpression);
                            if (value instanceof Number) {
                                return ((Number) value).doubleValue();
                            }
                            return 0.0;
                        })
                        .sum();

                item.add(new Label(componentId, "Sum: " + sum));
            }

            @Override
            public Object getData(ReadinessDetailWithDate object) {
                return getObjectAsString(object, propertyExpression);
            }
        };
    }

    private AggregatingPropertyColumn<ReadinessDetailWithDate, String> createColumnWithCountAndSum(String header, String propertyExpression) {
        return new AggregatingPropertyColumn<>(Model.of(header), propertyExpression, true, true) {
            @Override
            public void populateFooterCell(Item<ICellPopulator<ReadinessDetailWithDate>> item, String componentId,
                                           IDataProvider<ReadinessDetailWithDate> dataProvider) {
                // Use the filtered data directly
                filterData();
                List<ReadinessDetailWithDate> data = filteredData;

                // Count unique values
                long uniqueCount = data.stream()
                        .map(d -> getObjectAsString(d, propertyExpression))
                        .distinct()
                        .count();

                // Sum numeric values if applicable
                double sum = 0.0;
                boolean isNumeric = false;

                for (ReadinessDetailWithDate d : data) {
                    Object value = getObjectValue(d, propertyExpression);
                    if (value instanceof Number) {
                        isNumeric = true;
                        sum += ((Number) value).doubleValue();
                    }
                }

                String footerText = "Count: " + uniqueCount;
                if (isNumeric) {
                    footerText += ", Sum: " + sum;
                }

                item.add(new Label(componentId, footerText));
            }

            @Override
            public Object getData(ReadinessDetailWithDate object) {
                return getObjectAsString(object, propertyExpression);
            }
        };
    }

    private Object getObjectValue(ReadinessDetailWithDate data, String propertyExpression) {
        try {
            PropertyResolver.setValue(propertyExpression, data, null, null);
            return PropertyResolver.getValue(propertyExpression, data);
        } catch (Exception e) {
            return null;
        }
    }

    private String getObjectAsString(ReadinessDetailWithDate data, String propertyExpression) {
        try {
            // Use reflection or a library like Apache Commons BeanUtils to resolve nested properties
            return BeanUtils.getProperty(data, propertyExpression);
        } catch (Exception e) {
            return ""; // Return empty string if the property cannot be resolved
        }
    }

    private void filterData() {
        if (filterText == null || filterText.isEmpty()) {
            filteredData = new ArrayList<>(allData);
        } else {
            filteredData = allData.stream()
                    .filter(detail -> matchesFilter(detail, filterText.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (!"all".equals(filterDate)) {
            filteredData = filteredData.stream()
                    .filter(detail -> detail.getDate().toString().equals(filterDate))
                    .collect(Collectors.toList());
        }
    }

    private boolean matchesFilter(ReadinessDetailWithDate detailWithDate, String filter) {
        ReadinessDetail detail = detailWithDate.getDetail();
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
        for (ReadinessDetailWithDate detailWithDate : selectedRows.isEmpty() ? filteredData : selectedRows) {
            ReadinessDetail detail = detailWithDate.getDetail();
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    detailWithDate.getDate(),
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

    private class ReadinessDataProvider extends SortableDataProvider<ReadinessDetailWithDate, String> {
        private String sortProperty = "detail.productionPlant";
        private SortOrder sortOrder = SortOrder.ASCENDING;

        public ReadinessDataProvider() {
            setSort(sortProperty, sortOrder);
        }

        @Override
        public Iterator<? extends ReadinessDetailWithDate> iterator(long first, long count) {
            List<ReadinessDetailWithDate> sorted = new ArrayList<>(filteredData);
            sorted.sort((o1, o2) -> {
                int compare = compareByProperty(o1, o2, getSort().getProperty());
                return getSort().isAscending() ? compare : -compare;
            });

            return sorted.subList((int) first, (int) Math.min(first + count, sorted.size())).iterator();
        }

        @Override
        public long size() {
            return filteredData.size();
        }

        @Override
        public IModel<ReadinessDetailWithDate> model(ReadinessDetailWithDate object) {
            return Model.of(object);
        }

        private int compareByProperty(ReadinessDetailWithDate o1, ReadinessDetailWithDate o2, String property) {
            ReadinessDetail detail1 = o1.getDetail();
            ReadinessDetail detail2 = o2.getDetail();
            return switch (property) {
                case "detail.productionPlant" -> detail1.getProductionPlant().compareTo(detail2.getProductionPlant());
                case "detail.salesDocument" -> detail1.getSalesDocument().compareTo(detail2.getSalesDocument());
                case "detail.customerName" -> detail1.getCustomerName().compareTo(detail2.getCustomerName());
                // Add other properties as needed
                default -> 0;
            };
        }
    }
}