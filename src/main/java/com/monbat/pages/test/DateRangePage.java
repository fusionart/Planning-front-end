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
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DateRangePage extends Panel {
    private static final Logger LOG = LoggerFactory.getLogger(DateRangePage.class);

    private TabbedPanel<WeekTab> tabbedPanel;
    private final WebMarkupContainer tabsContainer;
    private final List<WeekTab> tabs = new ArrayList<>();
    private LocalDate startDateFilter;
    private LocalDate endDateFilter;

    // Progress components
    private final WebMarkupContainer progressContainer;
    private Label progressMessage;
    private Label progressPercentage;
    private WebMarkupContainer progressBarFill;

    private IModel<String> progressWidthModel = Model.of("0%");
    private IModel<String> progressStyleModel = Model.of("");

    // Loading state and timer reference
    private boolean isLoading = false;
    private AbstractAjaxTimerBehavior currentTimer = null;

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

        // Create progress container
        progressContainer = createProgressContainer();
        add(progressContainer);

        // Create apply button with timer-based progress
        form.add(createApplyButtonWithTimer());

        // Create tabs container
        tabsContainer = createTabsContainer();
        add(tabsContainer);
    }

    private WebMarkupContainer createProgressContainer() {
        WebMarkupContainer container = new WebMarkupContainer("progressContainer");
        container.setOutputMarkupId(true);
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(false);

        progressMessage = new Label("progressMessage", Model.of(""));
        progressMessage.setOutputMarkupId(true);
        container.add(progressMessage);

        progressPercentage = new Label("progressPercentage", Model.of("0%"));
        progressPercentage.setOutputMarkupId(true);
        container.add(progressPercentage);

        WebMarkupContainer progressBar = new WebMarkupContainer("progressBar");
        progressBar.setOutputMarkupId(true);
        container.add(progressBar);

        // Create progress bar fill with model-based styling
        progressBarFill = new WebMarkupContainer("progressBarFill") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                // Apply the style directly to the tag
                String currentStyle = progressStyleModel.getObject();
                if (currentStyle != null && !currentStyle.isEmpty()) {
                    tag.put("style", currentStyle);
                }
            }
        };
        progressBarFill.setOutputMarkupId(true);
        progressBar.add(progressBarFill);

        return container;
    }

    private AjaxButton createApplyButtonWithTimer() {
        return new AjaxButton("apply") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (isLoading) {
                    return;
                }
                startTimerBasedLoading(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                resetLoading(target);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(!isLoading);
            }
        };
    }

    private void startTimerBasedLoading(AjaxRequestTarget target) {
        // Stop any existing timer first
        if (currentTimer != null) {
            currentTimer.stop(target);
            remove(currentTimer);
            currentTimer = null;
        }

        // Reset state
        isLoading = true;
        tabs.clear();

        // Hide tabs and show progress
        tabsContainer.setVisible(false);
        progressContainer.setVisible(true);
        updateProgress(target, 5, "Starting data load...");

        // Create timer for progressive loading
        currentTimer = new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
            private int step = 0;
            private List<SalesOrder> salesOrders = new ArrayList<>();
            private List<PlannedOrder> plannedOrders = new ArrayList<>();
            private List<ProductionOrder> productionOrders = new ArrayList<>();

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                step++;

                try {
                    switch (step) {
                        case 1:
                            updateProgress(target, 15, "Loading sales orders from SAP...");
                            break;

                        case 2:
                            try {
                                salesOrders = SalesOrderApiClient.getData(startDateFilter, endDateFilter);
                                updateProgress(target, 40, "Sales orders loaded (" + salesOrders.size() + " found)");
                            } catch (Exception e) {
                                LOG.warn("Failed to load sales orders: {}", e.getMessage());
                                salesOrders = new ArrayList<>();
                                updateProgress(target, 40, "Sales orders load failed, continuing...");
                            }
                            break;

                        case 3:
                            updateProgress(target, 45, "Loading planned orders from SAP...");
                            break;

                        case 4:
                            try {
                                plannedOrders = PlannedOrderApiClient.getData(startDateFilter, endDateFilter);
                                updateProgress(target, 70, "Planned orders loaded (" + plannedOrders.size() + " found)");
                            } catch (Exception e) {
                                LOG.warn("Failed to load planned orders: {}", e.getMessage());
                                plannedOrders = new ArrayList<>();
                                updateProgress(target, 70, "Planned orders load failed, continuing...");
                            }
                            break;

                        case 5:
                            updateProgress(target, 75, "Loading production orders from SAP...");
                            break;

                        case 6:
                            try {
                                productionOrders = ProductionOrderApiClient.getData(startDateFilter, endDateFilter);
                                updateProgress(target, 90, "Production orders loaded (" + productionOrders.size() + " found)");
                            } catch (Exception e) {
                                LOG.warn("Failed to load production orders: {}", e.getMessage());
                                productionOrders = new ArrayList<>();
                                updateProgress(target, 90, "Production orders load failed, continuing...");
                            }
                            break;

                        case 7:
                            updateProgress(target, 95, "Creating tabs...");
                            break;

                        case 8:
                            try {
                                if (!salesOrders.isEmpty()) {
                                    createTabs(salesOrders, plannedOrders, productionOrders);
                                }
                                updateProgress(target, 100, "Complete!");
                            } catch (Exception e) {
                                LOG.error("Failed to create tabs", e);
                                updateProgress(target, 100, "Complete with errors!");
                            }
                            break;

                        default:
                            this.stop(target);
                            finishLoading(target, salesOrders);
                            return;
                    }

                } catch (Exception e) {
                    LOG.error("Critical error in loading step {}", step, e);
                    this.stop(target);
                    handleError(target, e);
                }
            }

            public void stop(AjaxRequestTarget target) {
                super.stop(target);
                if (currentTimer == this) {
                    currentTimer = null;
                }
            }
        };

        add(currentTimer);
        target.add(this);
    }

    private void updateProgress(AjaxRequestTarget target, int progress, String message) {
        if (progressMessage != null && progressPercentage != null && progressBarFill != null) {
            // Update text components
            progressMessage.setDefaultModelObject(message);
            progressPercentage.setDefaultModelObject(progress + "%");

            // Create the style string for the progress bar fill
            String fillStyle = String.format(
                    "width: %d%%; " +
                            "height: 100%%; " +
                            "background: linear-gradient(135deg, #ff6b6b 0%%, #ff8e53 50%%, #ffffff 100%%); " +
                            "transition: width 0.5s ease; " +
                            "box-shadow: 0 2px 8px rgba(255,107,107,0.4); " +
                            "border-radius: 10px; " +
                            "display: block;",
                    progress
            );

            // Apply the style attribute
            progressBarFill.add(org.apache.wicket.AttributeModifier.replace("style", fillStyle));

            // Also add a CSS class for additional styling control
            String cssClass = "progress-bar-fill progress-" + progress;
            progressBarFill.add(org.apache.wicket.AttributeModifier.replace("class", cssClass));

            // Add debug logging to verify the update
            LOG.debug("Progress updated to {}% with message: {}", progress, message);
            LOG.debug("Applied style: {}", fillStyle);

            // Make sure to add the components to the AJAX target
            target.add(progressMessage);
            target.add(progressPercentage);
            target.add(progressBarFill);
            target.add(progressContainer);
        }
    }

    private void finishLoading(AjaxRequestTarget target, List<SalesOrder> salesOrders) {
        isLoading = false;
        progressContainer.setVisible(false);

        if (!salesOrders.isEmpty() && !tabs.isEmpty()) {
            tabsContainer.setVisible(true);
            info("Loaded " + getDistinctWeeks(salesOrders).size() + " delivery weeks with " + salesOrders.size() + " sales orders");
        } else {
            tabs.clear();
            tabsContainer.setVisible(false);
            if (salesOrders.isEmpty()) {
                info("No orders found for the selected date range.");
            } else {
                info("No tabs could be created from the loaded data.");
            }
        }

        target.add(this);
    }

    private void handleError(AjaxRequestTarget target, Exception e) {
        resetLoading(target);
        error("Failed to load data: " + e.getMessage());
        target.add(this);
    }

    private void resetLoading(AjaxRequestTarget target) {
        if (currentTimer != null) {
            currentTimer.stop(target);
            remove(currentTimer);
            currentTimer = null;
        }

        isLoading = false;
        progressContainer.setVisible(false);
        target.add(progressContainer);
    }

    private WebMarkupContainer createTabsContainer() {
        WebMarkupContainer container = new WebMarkupContainer("tabsContainer");
        container.setOutputMarkupId(true);
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(false);

        tabbedPanel = new TabbedPanel<WeekTab>("tabs", tabs) {
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
        container.add(tabbedPanel);

        return container;
    }

    private void createTabs(List<SalesOrder> salesOrders,
                            List<PlannedOrder> plannedOrders,
                            List<ProductionOrder> productionOrders) {
        tabs.clear();

        if (salesOrders == null || salesOrders.isEmpty()) {
            return;
        }

        List<String> distinctWeeks = getDistinctWeeks(salesOrders);

        // Create tabs for each distinct week
        distinctWeeks.forEach(week -> {
            try {
                tabs.add(new WeekTab(week, salesOrders, plannedOrders, productionOrders));
            } catch (Exception e) {
                LOG.warn("Failed to create tab for week {}: {}", week, e.getMessage());
            }
        });

        if (!tabs.isEmpty()) {
            tabbedPanel.setSelectedTab(0);
        }
    }

    private List<String> getDistinctWeeks(List<SalesOrder> salesOrders) {
        if (salesOrders == null || salesOrders.isEmpty()) {
            return new ArrayList<>();
        }

        return salesOrders.stream()
                .filter(order -> order != null && order.getRequestedDeliveryWeek() != null)
                .map(SalesOrder::getRequestedDeliveryWeek)
                .distinct()
                .sorted(this::compareWeeks)
                .toList();
    }

    private int compareWeeks(String week1, String week2) {
        try {
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
        } catch (Exception e) {
            LOG.warn("Failed to compare weeks {} and {}: {}", week1, week2, e.getMessage());
            return week1.compareTo(week2);
        }
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
            if (items == null) {
                return new ArrayList<>();
            }

            return items.stream()
                    .filter(item -> {
                        try {
                            LocalDate date = getDateFromItem(item);
                            return date != null && isInWeek(date, week);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        private LocalDate getDateFromItem(Object item) {
            try {
                if (item instanceof SalesOrder) {
                    SalesOrder order = (SalesOrder) item;
                    if (order.getRequestedDeliveryDate() != null) {
                        return order.getRequestedDeliveryDate().toLocalDate();
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        private boolean isInWeek(LocalDate date, String weekStr) {
            try {
                String[] parts = weekStr.split("/");
                int weekNum = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);

                return date.get(WeekFields.ISO.weekOfWeekBasedYear()) == weekNum
                        && date.getYear() == year;
            } catch (Exception e) {
                return false;
            }
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