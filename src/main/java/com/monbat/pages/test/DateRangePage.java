package com.monbat.pages.test;

import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.form.datepicker.DatePicker;
import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import com.monbat.pages.salesOrders.SalesOrderPanel;
import com.monbat.services.api.PlannedOrderApiClient;
import com.monbat.services.api.ProductionOrderApiClient;
import com.monbat.services.api.SalesOrderApiClient;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DateRangePage extends Panel {
    private static final Logger LOG = LoggerFactory.getLogger(DateRangePage.class);

    private final TabbedPanel<WeekTab> tabbedPanel;
    private final List<WeekTab> tabs = new ArrayList<>();
    private LocalDate startDateFilter;
    private LocalDate endDateFilter;
    private final WebMarkupContainer loadingContainer;
    private final Label loadingMessage;

    public DateRangePage(String id) {
        super(id);
        setOutputMarkupId(true);

        // Create form with date pickers
        Form<Void> form = new Form<>("form");
        add(form);

        startDateFilter = LocalDate.now().minusMonths(1);
        endDateFilter = LocalDate.now();

        // Initialize date pickers and add them to the form
        DatePicker startDatePicker = getStartDatePicker();
        DatePicker endDatePicker = getEndDatePicker();

        form.add(startDatePicker);
        form.add(endDatePicker);

        // Loading message components
        loadingContainer = new WebMarkupContainer("loadingContainer");
        loadingContainer.setOutputMarkupId(true);
        loadingContainer.setVisible(false);

        loadingMessage = new Label("loadingMessage", Model.of("Loading data..."));
        loadingMessage.setOutputMarkupId(true);
        loadingContainer.add(loadingMessage);

        add(loadingContainer);

        form.add(new AjaxButton("apply") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                // Show loading message
                loadingContainer.setVisible(true);
                loadingMessage.setDefaultModelObject("Loading order data...");
                target.add(loadingContainer);

                // Load data immediately (no need for setTimeout)
                loadOrderData(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                loadingMessage.setDefaultModelObject("Error occurred during form submission");
                target.add(loadingMessage);
            }
        });

        tabbedPanel = new TabbedPanel<>("tabs", tabs) {
            @Override
            protected WebMarkupContainer newLink(String linkId, final int index) {
                return new AjaxLink<Void>(linkId) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setSelectedTab(index);
                        if (target != null) {
                            target.add(tabbedPanel);
                        }
                    }
                };
            }

            @Override
            protected String getSelectedTabCssClass() {
                return "active";
            }

            @Override
            protected String getLastTabCssClass() {
                return "";
            }
        };
        tabbedPanel.setOutputMarkupId(true);
        add(tabbedPanel);
    }

    private void loadOrderData(AjaxRequestTarget target) {
        try {
            // Update loading message
            loadingMessage.setDefaultModelObject("Fetching sales orders...");
            target.add(loadingMessage);

            // Fetch data from APIs
            List<SalesOrder> salesOrders = SalesOrderApiClient.getData(startDateFilter, endDateFilter);

            loadingMessage.setDefaultModelObject("Fetching planned orders...");
            target.add(loadingMessage);
            List<PlannedOrder> plannedOrders = PlannedOrderApiClient.getData(startDateFilter, endDateFilter);

            loadingMessage.setDefaultModelObject("Fetching production orders...");
            target.add(loadingMessage);
            List<ProductionOrder> productionOrders = ProductionOrderApiClient.getData(startDateFilter, endDateFilter);

            if (!salesOrders.isEmpty()) {
                loadingMessage.setDefaultModelObject("Creating tabs...");
                target.add(loadingMessage);
                updateTabs(salesOrders, plannedOrders, productionOrders);

                // Hide loading message when done
                loadingContainer.setVisible(false);
                target.add(loadingContainer);
                target.add(tabbedPanel);
            } else {
                tabs.clear();
                loadingMessage.setDefaultModelObject("No orders found for the selected date range.");
                target.add(loadingMessage);
                info("No orders found for the selected date range.");
            }
        } catch (Exception e) {
            LOG.error("Error loading order data", e);
            loadingMessage.setDefaultModelObject("Failed to load data: " + e.getMessage());
            target.add(loadingMessage);
            error("Failed to load data: " + e.getMessage());
        }
    }

    private void updateTabs(List<SalesOrder> salesOrders,
                            List<PlannedOrder> plannedOrders,
                            List<ProductionOrder> productionOrders) {
        tabs.clear();

        // Create tabs for each distinct week
        getDistinctWeeks(salesOrders).forEach(week ->
                tabs.add(new WeekTab(week, salesOrders, plannedOrders, productionOrders))
        );

        if (!tabs.isEmpty()) {
            tabbedPanel.setSelectedTab(0);
        }
    }

    private List<String> getDistinctWeeks(List<SalesOrder> salesOrders) {
        return salesOrders.stream()
                .map(SalesOrder::getRequestedDeliveryWeek)
                .distinct()
                .sorted(this::compareWeeks)
                .toList();
    }

    private int compareWeeks(String week1, String week2) {
        String[] parts1 = week1.split("/");
        String[] parts2 = week2.split("/");

        int weekNum1 = Integer.parseInt(parts1[0]);
        int year1 = Integer.parseInt(parts1[1]);
        int weekNum2 = Integer.parseInt(parts2[0]);
        int year2 = Integer.parseInt(parts2[1]);

        if (year1 != year2) {
            return Integer.compare(year1, year2);
        }
        return Integer.compare(weekNum1, weekNum2);
    }

    private DatePicker getStartDatePicker() {
        Options options = new Options();
        options.set("dateFormat", "'dd.mm.yy'");
        options.set("firstDay", 1);
        options.set("changeMonth", true);
        options.set("changeYear", true);

        return new DatePicker("startDatePicker",
                new PropertyModel<>(this, "startDateFilter"), "dd.MM.yyyy", options);
    }

    private DatePicker getEndDatePicker() {
        Options options = new Options();
        options.set("dateFormat", "'dd.mm.yy'");
        options.set("firstDay", 1);
        options.set("changeMonth", true);
        options.set("changeYear", true);

        return new DatePicker("endDatePicker",
                new PropertyModel<>(this, "endDateFilter"), "dd.MM.yyyy", options);
    }

    private static class WeekTab implements ITab {
        private final String week;
        private final List<SalesOrder> salesOrders;
        private final List<PlannedOrder> plannedOrders;
        private final List<ProductionOrder> productionOrders;

        public WeekTab(String week,
                       List<SalesOrder> allSales,
                       List<PlannedOrder> allPlanned,
                       List<ProductionOrder> allProduction) {
            this.week = week;
            this.salesOrders = filterByWeek(allSales, week);
            this.plannedOrders = filterByWeek(allPlanned, week);
            this.productionOrders = filterByWeek(allProduction, week);
        }

        private <T> List<T> filterByWeek(List<T> items, String week) {
            return items.stream()
                    .filter(item -> {
                        LocalDate date = getDateFromItem(item);
                        return date != null && isInWeek(date, week);
                    })
                    .collect(Collectors.toList());
        }

        private LocalDate getDateFromItem(Object item) {
            if (item instanceof SalesOrder) {
                return ((SalesOrder) item).getRequestedDeliveryDate().toLocalDate();
            }
            return null;
        }

        private boolean isInWeek(LocalDate date, String weekStr) {
            String[] parts = weekStr.split("/");
            int weekNum = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);

            return date.get(WeekFields.ISO.weekOfWeekBasedYear()) == weekNum
                    && date.getYear() == year;
        }

        @Override
        public IModel<String> getTitle() {
            return Model.of("Week " + week);
        }

        @Override
        public Panel getPanel(String panelId) {
            return new SalesOrderPanel(panelId, salesOrders, plannedOrders, productionOrders);
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    }
}