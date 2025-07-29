package com.monbat.pages.salesOrders;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.components.genericTable.DynamicColumnDefinition;
import com.monbat.components.genericTable.PropertyColumnDefinition;
import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrderItem;
import com.monbat.models.dto.sap.sales_order.SalesOrderMain;
import com.monbat.models.dto.sap.sales_order.SalesOrderMainItem;
import com.monbat.models.entities.Material;
import com.monbat.services.api.MaterialApiClient;
import com.monbat.services.api.MaterialStockApiClient;
import com.monbat.utils.CustomDictionary;
import com.monbat.utils.enums.TableNames;
import org.apache.commons.lang3.StringUtils;
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

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SalesOrderTableWithDropdown extends Panel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<SalesOrder> allSalesOrders;
    private final List<PlannedOrder> plannedOrderList;
    private final List<ProductionOrder> productionOrderList;

    private List<SalesOrderMain> allData;
    private List<SalesOrderMain> filteredData;
    private String filterText = "";
    private String filterColumn = "all";
    private String selectedPlant = "All"; // Default to "All"
    private final DataTable<SalesOrderMain, String> dataTable;
    private final IModel<Integer> rowsPerPage = Model.of(10);
    private final List<ColumnDefinition<SalesOrderMain>> columnDefinitions;
    private final Function<SalesOrderMain, List<String>> filterFunction;

    public SalesOrderTableWithDropdown(String id,
                                       List<SalesOrder> salesOrders,
                                       List<PlannedOrder> plannedOrderList,
                                       List<ProductionOrder> productionOrderList) {
        super(id);
        setOutputMarkupId(true);

        this.allSalesOrders = salesOrders;
        this.plannedOrderList = plannedOrderList;
        this.productionOrderList = productionOrderList;

        // Generate all data initially
        this.allData = generateSalesOrderMainData(allSalesOrders, plannedOrderList, productionOrderList);
        this.filteredData = new ArrayList<>(allData);

        // Create column definitions
        this.columnDefinitions = createDynamicColumns();
        this.filterFunction = createSerializableFilterFunction();

        // Create the main form
        Form<?> form = new Form<>("form");
        add(form);

        // Add filter controls
        addFilterControls(form);

        // Add plant dropdown
        addPlantDropdown(form);

        // Add export button
        addExportButton(form);

        // Add rows per page selector
        addRowsPerPageSelector(form);

        // Add the data table
        dataTable = createDataTable("table");
        dataTable.setOutputMarkupId(true);

        WebMarkupContainer tableContainer = new WebMarkupContainer("tableContainer");
        tableContainer.setOutputMarkupId(true);
        tableContainer.add(dataTable);
        form.add(tableContainer);
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

    private void addPlantDropdown(Form<?> form) {
        // Create list of available plants
        List<String> availablePlants = new ArrayList<>();
        availablePlants.add("All"); // Default option

        List<String> distinctPlants = getDistinctPlants(allData);
        availablePlants.addAll(distinctPlants);

        DropDownChoice<String> plantDropdown = new DropDownChoice<>("plantDropdown",
                new PropertyModel<>(this, "selectedPlant"), availablePlants);
        plantDropdown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                filterData();
                target.add(dataTable);
            }
        });
        form.add(plantDropdown);
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

    private void updateDataForSelectedWeek() {
        // This method is no longer needed since we're filtering by plant, not changing the base data
        // The filtering is now handled in the filterData() method
    }

    private DataTable<SalesOrderMain, String> createDataTable(String id) {
        List<IColumn<SalesOrderMain, String>> columns = new ArrayList<>();

        // Add columns from definitions
        for (ColumnDefinition<SalesOrderMain> definition : columnDefinitions) {
            columns.add(definition.createColumn());
        }

        GenericDataProvider dataProvider = new GenericDataProvider();
        return new DefaultDataTable<>(id, columns, dataProvider, rowsPerPage.getObject());
    }

    private void filterData() {
        List<SalesOrderMain> dataToFilter = new ArrayList<>(allData);

        // First filter by selected plant
        if (!"All".equals(selectedPlant)) {
            dataToFilter = dataToFilter.stream()
                    .filter(item -> selectedPlant.equals(item.getPlant()))
                    .collect(Collectors.toList());
        }

        // Then apply text filter if present
        if (filterText == null || filterText.isEmpty()) {
            filteredData = dataToFilter;
        } else {
            filteredData = dataToFilter.stream()
                    .filter(this::matchesFilter)
                    .collect(Collectors.toList());
        }
    }

    private boolean matchesFilter(SalesOrderMain item) {
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
    }

    private List<SalesOrderMain> generateSalesOrderMainData(List<SalesOrder> filteredSalesOrders,
                                                            List<PlannedOrder> plannedOrderList,
                                                            List<ProductionOrder> productionOrderList) {
        List<SalesOrderMain> localSalesOrderMainList = new ArrayList<>();
        List<Material> materialList = MaterialApiClient.getData();

        for (SalesOrder salesOrder : filteredSalesOrders) {
            for (SalesOrderItem salesOrderItem : salesOrder.getToItem()) {
                boolean exists = localSalesOrderMainList.stream()
                        .anyMatch(item -> salesOrderItem.getMaterial().equals(item.getMaterial()));

                String plannedOrder = plannedOrderList.stream()
                        .filter(material -> material.getMaterial().equals(salesOrderItem.getMaterial()))
                        .filter(so -> so.getSalesOrder().equals(salesOrder.getSalesOrderNumber()))
                        .findFirst()
                        .map(PlannedOrder::getPlannedOrder)
                        .orElse("");

                String productionOrder = productionOrderList.stream()
                        .filter(material -> material.getMaterial().equals(salesOrderItem.getMaterial()))
                        .filter(so -> so.getSalesOrder().equals(salesOrder.getSalesOrderNumber()))
                        .findFirst()
                        .map(ProductionOrder::getProductionOrder)
                        .orElse("");

                if (!exists) {
                    String plantName = getPlantName(salesOrderItem, materialList);
                    double notChargedQuantity =
                            MaterialStockApiClient.getData("20" + StringUtils.substring(salesOrderItem.getMaterial(), 2, salesOrderItem.getMaterial().length() - 1) + "2");
                    double chargedQuantity =
                            MaterialStockApiClient.getData("11" + StringUtils.right(salesOrderItem.getMaterial(), salesOrderItem.getMaterial().length() - 2));

                    SalesOrderMain salesOrderMain = new SalesOrderMain(salesOrderItem.getMaterial(),
                            salesOrderItem.getRequestedQuantity(), plantName,
                            salesOrderItem.getRequestedQuantityUnit(),
                            notChargedQuantity,
                            chargedQuantity);

                    salesOrderMain.addDynamicSoValue(salesOrder.getSalesOrderNumber(),
                            new SalesOrderMainItem(salesOrderItem.getRequestedQuantity(), plannedOrder, productionOrder));

                    localSalesOrderMainList.add(salesOrderMain);

                } else {
                    Optional<SalesOrderMain> foundItem = localSalesOrderMainList.stream()
                            .filter(item -> salesOrderItem.getMaterial().equals(item.getMaterial()))
                            .findFirst();

                    foundItem.ifPresent(item -> {
                        item.setRequestedQuantity(item.getRequestedQuantity() + salesOrderItem.getRequestedQuantity());
                        item.addDynamicSoValue(salesOrder.getSalesOrderNumber(),
                                new SalesOrderMainItem(salesOrderItem.getRequestedQuantity(), plannedOrder, productionOrder));
                    });
                }
            }
        }

        return localSalesOrderMainList;
    }

    private static String getPlantName(SalesOrderItem salesOrderItem, List<Material> materialList) {
        int plant = materialList.stream()
                .filter(material -> salesOrderItem.getMaterial().equals(material.getMaterial()))
                .findFirst()
                .map(Material::getPlant)
                .orElse(0);

        String plantName = switch (plant) {
            case 1000 -> "Monbat";
            case 1100 -> "Start";
            default -> "";
        };

        // Check for VRLA
        plantName = switch (StringUtils.left(salesOrderItem.getMaterial(), 4)) {
            case "1012", "102M", "104M", "106M", "108H" -> "RP";
            default -> plantName;
        };
        return plantName;
    }

    private List<ColumnDefinition<SalesOrderMain>> createDynamicColumns() {
        List<ColumnDefinition<SalesOrderMain>> columns = new ArrayList<>();
        Map<String, String> dictionary = CustomDictionary.getDictionary(TableNames.SALES_ORDER_WITH_ITEM);

        // Add standard property columns from SalesOrderMain (excluding dynamicSoItems)
        Field[] fields = SalesOrderMain.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();

            // Skip fields we don't want to display
            if (shouldSkipField(fieldName)) continue;

            assert dictionary != null;
            String displayName = dictionary.getOrDefault(fieldName, fieldName);
            columns.add(new PropertyColumnDefinition<>(
                    displayName,
                    fieldName,
                    true,  // sortable
                    isNumeric(field.getType())  // aggregatable
            ));
        }

        // Get all unique sales order numbers from the current data to create dynamic columns
        Set<String> salesOrderNumbers = extractUniqueSalesOrderNumbers();

        // Create dynamic columns for each sales order number
        for (String salesOrderNumber : salesOrderNumbers) {
            // Add three subcolumns for each sales order
            columns.add(new DynamicColumnDefinition(salesOrderNumber, "quantity"));
            columns.add(new DynamicColumnDefinition(salesOrderNumber, "plannedOrder"));
            columns.add(new DynamicColumnDefinition(salesOrderNumber, "productionOrder"));
        }

        return columns;
    }

    private Set<String> extractUniqueSalesOrderNumbers() {
        Set<String> salesOrderNumbers = new TreeSet<>(); // TreeSet for natural ordering

        // Extract from all sales orders
        salesOrderNumbers.addAll(
                allSalesOrders.stream()
                        .map(SalesOrder::getSalesOrderNumber)
                        .collect(Collectors.toSet())
        );

        return salesOrderNumbers;
    }

    private boolean shouldSkipField(String fieldName) {
        return fieldName.equals("dynamicSoItems");
    }

    private boolean isNumeric(Class<?> type) {
        return Number.class.isAssignableFrom(type) ||
                type == double.class ||
                type == int.class ||
                type == long.class ||
                type == float.class;
    }

    private Function<SalesOrderMain, List<String>> createSerializableFilterFunction() {
        return new SerializableFunction<>() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public List<String> apply(SalesOrderMain item) {
                List<String> searchableValues = new ArrayList<>();

                // Add all standard fields
                Field[] fields = SalesOrderMain.class.getDeclaredFields();
                for (Field field : fields) {
                    if (shouldSkipField(field.getName())) continue;

                    try {
                        field.setAccessible(true);
                        Object value = field.get(item);
                        searchableValues.add(value != null ? value.toString() : "");
                    } catch (IllegalAccessException e) {
                        searchableValues.add("");
                    }
                }

                // Add dynamic column values for filtering
                if (item.getDynamicSoItems() != null) {
                    for (SalesOrderMainItem dynamicItem : item.getDynamicSoItems().values()) {
                        searchableValues.add(dynamicItem.getQuantity() != null ? dynamicItem.getQuantity().toString() : "");
                        searchableValues.add(dynamicItem.getPlannedOrder() != null ? dynamicItem.getPlannedOrder() : "");
                        searchableValues.add(dynamicItem.getProductionOrder() != null ? dynamicItem.getProductionOrder() : "");
                    }
                }

                return searchableValues;
            }
        };
    }

    private List<String> getDistinctPlants(List<SalesOrderMain> salesOrderMainList) {
        return salesOrderMainList.stream()
                .map(SalesOrderMain::getPlant)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private static abstract class SerializableFunction<T, R> implements Function<T, R>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private class GenericDataProvider extends SortableDataProvider<SalesOrderMain, String> {
        @Override
        public Iterator<? extends SalesOrderMain> iterator(long first, long count) {
            List<SalesOrderMain> data = new ArrayList<>(filteredData);

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
        public IModel<SalesOrderMain> model(SalesOrderMain object) {
            return Model.of(object);
        }
    }
}