package com.monbat.pages.test;

import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.markup.html.panel.Panel;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WeekTab implements ITab {
    private final String week;
    private final List<SalesOrder> salesOrders;
    private final List<PlannedOrder> plannedOrders;
    private final List<ProductionOrder> productionOrders;

    public WeekTab(String week,
                   List<SalesOrder> allSalesOrders,
                   List<PlannedOrder> allPlannedOrders,
                   List<ProductionOrder> allProductionOrders) {
        this.week = week;

        // Filter orders for this specific week
        this.salesOrders = allSalesOrders.stream()
                .filter(so -> so.getRequestedDeliveryWeek().equals(week))
                .collect(Collectors.toList());

//        this.plannedOrders = allPlannedOrders.stream()
//                .filter(po -> isInWeek(po.getDate(), week))
//                .collect(Collectors.toList());
//
//        this.productionOrders = allProductionOrders.stream()
//                .filter(po -> isInWeek(po.getDate(), week))
//                .collect(Collectors.toList());
        this.plannedOrders = new ArrayList<>();
        this.productionOrders = new ArrayList<>();
    }

    private boolean isInWeek(LocalDate date, String weekString) {
        String[] parts = weekString.split("/");
        int weekNumber = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        return date.get(WeekFields.ISO.weekOfWeekBasedYear()) == weekNumber
                && date.getYear() == year;
    }

    @Override
    public IModel<String> getTitle() {
        return Model.of("Week " + week);
    }

    @Override
    public Panel getPanel(String panelId) {
        return new WeekPanel(panelId, week, salesOrders, plannedOrders, productionOrders);
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
