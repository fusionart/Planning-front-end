package com.monbat.components.genericTable;

import com.monbat.models.dto.sap.sales_order.SalesOrderMain;
import com.monbat.models.dto.sap.sales_order.SalesOrderMainItem;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * Custom column definition for dynamic columns based on SalesOrderMain.dynamicSoItems
 */
public class DynamicColumnDefinition implements ColumnDefinition<SalesOrderMain>, Serializable {
    private final String salesOrderNumber;
    private final String subColumnType; // "quantity", "plannedOrder", or "productionOrder"
    private final String header;

    public DynamicColumnDefinition(String salesOrderNumber, String subColumnType) {
        this.salesOrderNumber = salesOrderNumber;
        this.subColumnType = subColumnType;
        this.header = createHeader(salesOrderNumber, subColumnType);
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
        return new AbstractColumn<SalesOrderMain, String>(Model.of(header), null) {
            @Override
            public void populateItem(Item<ICellPopulator<SalesOrderMain>> cellItem,
                                     String componentId, IModel<SalesOrderMain> rowModel) {

                SalesOrderMain salesOrderMain = rowModel.getObject();
                String value = extractValue(salesOrderMain);

                cellItem.add(new Label(componentId, value));
            }
        };
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
}