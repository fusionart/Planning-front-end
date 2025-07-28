package com.monbat.pages.salesOrders;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.components.genericTable.GenericDataTablePanel;
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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SalesOrderPanel extends Panel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<SalesOrder> salesOrders;
    private final List<PlannedOrder> plannedOrderList;
    private final List<ProductionOrder> productionOrderList;
    private final List<TabInfo> tabs;
    private int activeTabIndex = 0;

    public SalesOrderPanel(String id, List<SalesOrder> items, List<PlannedOrder> plannedOrderList, List<ProductionOrder> productionOrderList) {
        super(id);
        setOutputMarkupId(true);

        this.salesOrders = items;
        this.plannedOrderList = plannedOrderList;
        this.productionOrderList = productionOrderList;
        this.tabs = createTabs();

        // Create tab navigation with modern styling
        ListView<TabInfo> tabList = new ListView<TabInfo>("tabList", tabs) {
            @Override
            protected void populateItem(ListItem<TabInfo> item) {
                TabInfo tab = item.getModelObject();
                int tabIndex = item.getIndex();

                AjaxLink<Void> tabLink = new AjaxLink<Void>("tabLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        activeTabIndex = tabIndex;
                        target.add(SalesOrderPanel.this);
                    }
                };

                // Apply modern CSS classes
                item.add(new AttributeModifier("class", "modern-tab-item"));

                // Add active class if this is the active tab
                if (tabIndex == activeTabIndex) {
                    tabLink.add(new AttributeModifier("class", "modern-tab-link active"));
                } else {
                    tabLink.add(new AttributeModifier("class", "modern-tab-link"));
                }

                Label tabTitle = new Label("tabTitle", tab.getTitle());
                tabLink.add(tabTitle);
                item.add(tabLink);
            }
        };
        add(tabList);

        // Create active tab content
        createActiveTabContent();
    }

    private void createActiveTabContent() {
        TabInfo activeTab = tabs.get(activeTabIndex);
        IModel<Collection<SalesOrderMain>> model = () -> generateSalesOrderMainData(
                activeTab.getSalesOrders(), plannedOrderList, productionOrderList);

        // Create columns dynamically
        List<ColumnDefinition<SalesOrderMain>> columns = createDynamicColumns();

        // Create filter function
        Function<SalesOrderMain, List<String>> filterFunction = createSerializableFilterFunction();

        GenericDataTablePanel<SalesOrderMain> dataTablePanel = new GenericDataTablePanel<>(
                "activeTabContent",
                model,
                columns,
                filterFunction
        );

        // Remove existing content if any
        if (get("activeTabContent") != null) {
            remove("activeTabContent");
        }

        dataTablePanel.setOutputMarkupId(true);
        add(dataTablePanel);
    }

    private List<TabInfo> createTabs() {
        List<TabInfo> tabList = new ArrayList<>();

        // Group sales orders by plant or another meaningful criteria
        Map<String, List<SalesOrder>> groupedOrders = salesOrders.stream()
                .collect(Collectors.groupingBy(order -> getPlantName(order)));

        // If no meaningful grouping, create a single "All Orders" tab
        if (groupedOrders.size() <= 1) {
            tabList.add(new TabInfo("All Orders", salesOrders));
        } else {
            // Create tabs for each group
            for (Map.Entry<String, List<SalesOrder>> entry : groupedOrders.entrySet()) {
                tabList.add(new TabInfo(entry.getKey(), entry.getValue()));
            }
        }

        return tabList;
    }

    private String getPlantName(SalesOrder order) {
        if (order.getToItem() != null && !order.getToItem().isEmpty()) {
            SalesOrderItem firstItem = order.getToItem().get(0);
            return getPlantName(firstItem, MaterialApiClient.getData());
        }
        return "Unknown";
    }

    private List<SalesOrderMain> generateSalesOrderMainData(List<SalesOrder> filteredSalesOrders,
                                                            List<PlannedOrder> plannedOrderList, List<ProductionOrder> productionOrderList) {
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
                        item.addDynamicSoValue(salesOrder.getSalesOrderNumber(), new SalesOrderMainItem(salesOrderItem.getRequestedQuantity(), plannedOrder, productionOrder));
                    });
                }
            }
        }

        return localSalesOrderMainList;
    }

    private static String getPlantName(SalesOrderItem salesOrderItem, List<Material> materialList) {
        String plantName;
        int plant = materialList.stream()
                .filter(material -> salesOrderItem.getMaterial().equals(material.getMaterial()))
                .findFirst()
                .map(Material::getPlant)
                .orElse(0);

        if (plant == 1000) {
            plantName = "Monbat";
        } else {
            plantName = "Start";
        }
        //check for VRLA
        plantName = switch (StringUtils.left(salesOrderItem.getMaterial(), 4)) {
            case "1012", "102M", "104M", "106M", "108H" -> "RP";
            default -> plantName;
        };
        return plantName;
    }

    private List<ColumnDefinition<SalesOrderMain>> createDynamicColumns() {
        List<ColumnDefinition<SalesOrderMain>> columns = new ArrayList<>();
        Map<String, String> dictionary = CustomDictionary.getDictionary(TableNames.SALES_ORDER_WITH_ITEM);

        // Add standard property columns from SalesOrderItemRow
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

        return columns;
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
                    try {
                        field.setAccessible(true);
                        Object value = field.get(item);
                        searchableValues.add(value != null ? value.toString() : "");
                    } catch (IllegalAccessException e) {
                        searchableValues.add("");
                    }
                }
                return searchableValues;
            }
        };
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        createActiveTabContent();
    }

    private static abstract class SerializableFunction<T, R> implements Function<T, R>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    // Inner class to represent tab information
    private static class TabInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String title;
        private final List<SalesOrder> salesOrders;

        public TabInfo(String title, List<SalesOrder> salesOrders) {
            this.title = title;
            this.salesOrders = salesOrders;
        }

        public String getTitle() {
            return title;
        }

        public List<SalesOrder> getSalesOrders() {
            return salesOrders;
        }
    }
}