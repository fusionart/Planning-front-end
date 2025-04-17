package com.monbat.pages.salesOrders;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.models.dto.sap.SalesOrderItemRow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialTotalColumnDefinition implements ColumnDefinition<SalesOrderItemRow>, Serializable {
    private final String title;
    private final boolean sortable;
    private final boolean filterable;
    private final Map<String, Double> materialTotals;

    public MaterialTotalColumnDefinition(String title, List<SalesOrderItemRow> allItems, boolean sortable, boolean filterable) {
        this.title = title;
        this.sortable = sortable;
        this.filterable = filterable;
        this.materialTotals = calculateMaterialTotals(allItems);
    }

    @Override
    public IColumn<SalesOrderItemRow, String> createColumn() {
        return new AbstractColumn<SalesOrderItemRow, String>(Model.of(title)) {
            @Override
            public void populateItem(Item<ICellPopulator<SalesOrderItemRow>> cellItem, String componentId,
                                     IModel<SalesOrderItemRow> rowModel) {
                SalesOrderItemRow row = rowModel.getObject();
                String material = row.getItem().getMaterial();
                Double total = materialTotals.getOrDefault(material, 0.0);
                cellItem.add(new Label(componentId, String.valueOf(total)));
            }
        };
    }

    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getPropertyExpression() {
        return "materialTotal"; // This is just a placeholder property expression
    }

    private Map<String, Double> calculateMaterialTotals(List<SalesOrderItemRow> items) {
        Map<String, Double> totals = new HashMap<>();

        for (SalesOrderItemRow item : items) {
            String material = item.getItem().getMaterial();
            try {
                double quantity = item.getItem().getRequestedQuantity();
                totals.put(material, totals.getOrDefault(material, 0.0) + quantity);
            } catch (NumberFormatException e) {
                // Handle invalid quantity values
                if (!totals.containsKey(material)) {
                    totals.put(material, 0.0);
                }
            }
        }

        return totals;
    }
}