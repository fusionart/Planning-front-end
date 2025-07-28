package com.monbat.pages.test;

import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeekPanel extends Panel {
    public WeekPanel(String id, String week,
                     List<SalesOrder> salesOrders,
                     List<PlannedOrder> plannedOrders,
                     List<ProductionOrder> productionOrders) {
        super(id);

        add(new Label("weekLabel", "Week: " + week));

        // Sales orders list for the week
        add(new ListView<>("salesOrders", salesOrders) {
            @Override
            protected void populateItem(ListItem<SalesOrder> item) {
                SalesOrder so = item.getModelObject();
                item.add(new Label("orderNumber", so.getSalesOrderNumber()));
                item.add(new Label("customer", so.getSoldToParty()));
                item.add(new Label("deliveryDate",
                        so.getRequestedDeliveryDate().format(DateTimeFormatter.ISO_DATE)));
            }
        });

        // Similar implementations for plannedOrders and productionOrders
        // ...
    }
}
