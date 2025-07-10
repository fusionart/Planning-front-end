package com.monbat.pages.test;

import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DayPanel extends Panel {
    public DayPanel(String id, LocalDate date,
                    List<SalesOrder> salesOrders,
                    List<PlannedOrder> plannedOrders,
                    List<ProductionOrder> productionOrders) {
        super(id);

        add(new Label("dayLabel", "Data for: " + date.format(DateTimeFormatter.ISO_DATE)));

        // Sales orders list
        add(new ListView<SalesOrder>("salesOrders", salesOrders) {
            @Override
            protected void populateItem(ListItem<SalesOrder> item) {
                SalesOrder so = item.getModelObject();
                item.add(new Label("orderNumber", so.getSalesOrderNumber()));
                item.add(new Label("customer", so.getSoldToParty()));
            }
        });

        // Planned orders list
        add(new ListView<PlannedOrder>("plannedOrders", plannedOrders) {
            @Override
            protected void populateItem(ListItem<PlannedOrder> item) {
                PlannedOrder po = item.getModelObject();
                item.add(new Label("plannedNumber", po.getPlannedOrder()));
                item.add(new Label("material", po.getMaterial()));
                item.add(new Label("plannedQuantity", po.getTotalQuantity()));
            }
        });

        // Production orders list
        add(new ListView<ProductionOrder>("productionOrders", productionOrders) {
            @Override
            protected void populateItem(ListItem<ProductionOrder> item) {
                ProductionOrder po = item.getModelObject();
                item.add(new Label("productionNumber", po.getProductionOrder()));
                item.add(new Label("actualQuantity", po.getTotalQuantity()));
            }
        });
    }
}