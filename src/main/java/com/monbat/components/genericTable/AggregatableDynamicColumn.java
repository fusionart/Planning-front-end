package com.monbat.components.genericTable;

import com.monbat.models.dto.sap.sales_order.SalesOrderMain;
import com.monbat.models.dto.sap.sales_order.SalesOrderMainItem;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * A custom column that can be easily identified for aggregation
 */
public class AggregatableDynamicColumn extends AbstractColumn<SalesOrderMain, String> implements Serializable {
    private final String salesOrderNumber;
    private final String subColumnType;
    private final boolean aggregatable;

    public AggregatableDynamicColumn(String salesOrderNumber, String subColumnType, boolean aggregatable) {
        super(Model.of(createHeader(salesOrderNumber, subColumnType)), null);
        this.salesOrderNumber = salesOrderNumber;
        this.subColumnType = subColumnType;
        this.aggregatable = aggregatable;
    }

    private static String createHeader(String salesOrderNumber, String subColumnType) {
        String subHeader = switch (subColumnType) {
            case "quantity" -> "Qty";
            case "plannedOrder" -> "Planned Order";
            case "productionOrder" -> "Production Order";
            default -> subColumnType;
        };
        return salesOrderNumber + " - " + subHeader;
    }

    @Override
    public void populateItem(Item<ICellPopulator<SalesOrderMain>> cellItem,
                             String componentId, IModel<SalesOrderMain> rowModel) {
        SalesOrderMain salesOrderMain = rowModel.getObject();
        String value = extractValue(salesOrderMain);
        cellItem.add(new Label(componentId, value));
    }

    private String extractValue(SalesOrderMain salesOrderMain) {
        if (salesOrderMain == null || salesOrderMain.getDynamicSoItems() == null) {
            return "";
        }

        SalesOrderMainItem item = salesOrderMain.getDynamicSoValue(salesOrderNumber);
        if (item == null) {
            return "";
        }

        return switch (subColumnType) {
            case "quantity" -> item.getQuantity() != null ? item.getQuantity().toString() : "";
            case "plannedOrder" -> item.getPlannedOrder() != null ? item.getPlannedOrder() : "";
            case "productionOrder" -> item.getProductionOrder() != null ? item.getProductionOrder() : "";
            default -> "";
        };
    }

    // Getters for the aggregation toolbar
    public String getSalesOrderNumber() {
        return salesOrderNumber;
    }

    public String getSubColumnType() {
        return subColumnType;
    }

    public boolean isAggregatable() {
        return aggregatable;
    }

    public String getHeader() {
        return String.valueOf(getDisplayModel().getObject());
    }

    // Method to extract the numeric value for aggregation
    public Double getNumericValue(SalesOrderMain salesOrderMain) {
        if (!aggregatable || salesOrderMain == null || salesOrderMain.getDynamicSoItems() == null) {
            return null;
        }

        SalesOrderMainItem item = salesOrderMain.getDynamicSoValue(salesOrderNumber);
        if (item == null || item.getQuantity() == null) {
            return null;
        }

        return item.getQuantity();
    }
}