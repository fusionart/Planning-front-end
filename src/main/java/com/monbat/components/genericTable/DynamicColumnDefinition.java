package com.monbat.components.genericTable;

import com.monbat.models.dto.sap.sales_order.SalesOrderMain;
import com.monbat.models.dto.sap.sales_order.SalesOrderMainItem;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

import java.io.Serializable;

/**
 * Custom column definition for dynamic columns based on SalesOrderMain.dynamicSoItems
 */
public class DynamicColumnDefinition implements ColumnDefinition<SalesOrderMain>, Serializable {
    private final String salesOrderNumber;
    private final String subColumnType; // "quantity", "plannedOrder", or "productionOrder"
    private final String header;
    private final boolean aggregatable; // New field to indicate if this column should be aggregated

    public DynamicColumnDefinition(String salesOrderNumber, String subColumnType) {
        this.salesOrderNumber = salesOrderNumber;
        this.subColumnType = subColumnType;
        this.header = createHeader(salesOrderNumber, subColumnType);
        // Only quantity columns should be aggregatable
        this.aggregatable = "quantity".equals(subColumnType);
    }

    // Constructor with explicit aggregatable flag (for future flexibility)
    public DynamicColumnDefinition(String salesOrderNumber, String subColumnType, boolean aggregatable) {
        this.salesOrderNumber = salesOrderNumber;
        this.subColumnType = subColumnType;
        this.header = createHeader(salesOrderNumber, subColumnType);
        this.aggregatable = aggregatable;
    }

    private String createHeader(String salesOrderNumber, String subColumnType) {
        String subHeader = switch (subColumnType) {
            case "quantity" -> "Qty";
            case "plannedOrder" -> "Planned Order";
            case "productionOrder" -> "Production Order";
            default -> subColumnType;
        };
        return salesOrderNumber + " - " + subHeader;
    }

    @Override
    public IColumn<SalesOrderMain, String> createColumn() {
        // Use the new AggregatableDynamicColumn instead of AbstractColumn
        return new AggregatableDynamicColumn(salesOrderNumber, subColumnType, aggregatable);
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

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getPropertyExpression() {
        // Return a property expression that can be used for sorting if needed
        return "dynamicSoItems." + salesOrderNumber + "." + subColumnType;
    }

    // Getter methods for the toolbar to access these values
    public String getSalesOrderNumber() {
        return salesOrderNumber;
    }

    public String getSubColumnType() {
        return subColumnType;
    }

    public boolean isAggregatable() {
        return aggregatable;
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