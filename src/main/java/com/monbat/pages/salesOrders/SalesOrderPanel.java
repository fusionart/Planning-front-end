package com.monbat.pages.salesOrders;

import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class SalesOrderPanel extends Panel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<SalesOrder> salesOrders;
    private final List<PlannedOrder> plannedOrderList;
    private final List<ProductionOrder> productionOrderList;

    public SalesOrderPanel(String id, List<SalesOrder> items, List<PlannedOrder> plannedOrderList, List<ProductionOrder> productionOrderList) {
        super(id);
        setOutputMarkupId(true);

        this.salesOrders = items;
        this.plannedOrderList = plannedOrderList;
        this.productionOrderList = productionOrderList;

        // Create the enhanced table panel with dropdown support
        SalesOrderTableWithDropdown tablePanel = new SalesOrderTableWithDropdown(
                "tablePanel",
                items,
                plannedOrderList,
                productionOrderList
        );

        add(tablePanel);
    }
}