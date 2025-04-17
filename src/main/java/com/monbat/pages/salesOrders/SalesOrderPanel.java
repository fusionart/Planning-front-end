package com.monbat.pages.salesOrders;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.components.genericTable.GenericDataTablePanel;
import com.monbat.components.genericTable.PropertyColumnDefinition;
import com.monbat.models.dto.sap.SalesOrderItemRow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.io.Serializable;
import java.util.*;

public class SalesOrderPanel extends Panel implements Serializable {
    public SalesOrderPanel(String id, List<SalesOrderItemRow> items) {
        super(id);
        setOutputMarkupId(true);

        IModel<Collection<SalesOrderItemRow>> model = () -> items;

        GenericDataTablePanel<SalesOrderItemRow> dataTablePanel = getComponents(model, items);
        add(dataTablePanel);
    }

    private static GenericDataTablePanel<SalesOrderItemRow> getComponents(
            IModel<Collection<SalesOrderItemRow>> flattenedModel,
            List<SalesOrderItemRow> allItems) {

        // Calculate the total quantity for each unique material
        Map<String, Double> materialTotals = calculateMaterialTotals(allItems);

        List<ColumnDefinition<SalesOrderItemRow>> flattenedColumns = new ArrayList<>();

        // Add existing columns
        flattenedColumns.add(new PropertyColumnDefinition<>("SalesOrder", "salesOrderNumber", true, false));
        flattenedColumns.add(new PropertyColumnDefinition<>("SoldToParty", "soldToParty", true, false));
        flattenedColumns.add(new PropertyColumnDefinition<>("RequestedDeliveryDate", "requestedDeliveryDate", true, false));
        flattenedColumns.add(new PropertyColumnDefinition<>("RequestedDeliveryWeek", "requestedDeliveryWeek", true, false));
        flattenedColumns.add(new PropertyColumnDefinition<>("Material", "material", true, false));
        flattenedColumns.add(new PropertyColumnDefinition<>("Quantity", "quantityWithUnit", true, false));

        // Add our custom column for material totals
        flattenedColumns.add(new MaterialTotalColumnDefinition("Total Material Quantity", allItems, true, false));

        flattenedColumns.add(new PropertyColumnDefinition<>("Status", "sDProcessStatus", true, false));

        // Create filter function with our custom column value
        return new GenericDataTablePanel<>(
                "table",
                flattenedModel,
                flattenedColumns,
                detail -> {
                    String material = detail.getItem().getMaterial();
                    Double total = materialTotals.getOrDefault(material, 0.0);

                    return Arrays.asList(
                            String.valueOf(detail.getOrder().getSalesOrderNumber()),
                            String.valueOf(detail.getOrder().getSoldToParty()),
                            String.valueOf(detail.getOrder().getRequestedDeliveryDate()),
                            String.valueOf(detail.getOrder().getRequestedDeliveryWeek()),
                            String.valueOf(detail.getItem().getMaterial()),
                            String.valueOf(detail.getItem().getRequestedQuantity()),
                            String.valueOf(total),
                            String.valueOf(detail.getSDProcessStatus())
                    );
                }
        );
    }

    private static Map<String, Double> calculateMaterialTotals(List<SalesOrderItemRow> items) {
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
