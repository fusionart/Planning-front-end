package com.monbat.pages.salesOrders;

import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.form.datepicker.DatePicker;
import com.googlecode.wicket.jquery.ui.panel.JQueryFeedbackPanel;
import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import com.monbat.services.api.PlannedOrderApiClient;
import com.monbat.services.api.ProductionOrderApiClient;
import com.monbat.services.api.SalesOrderApiClient;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SalesOrderDynamicTabsPage extends Panel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SalesOrderDynamicTabsPage.class);

    private final WebMarkupContainer loadingContainer;
    private Label loadingLabel;
    private FeedbackPanel feedback;
    private WebMarkupContainer tableContainer;
    private SalesOrderPanel salesOrderPanel;

    private LocalDate startDateFilter;
    private LocalDate endDateFilter;
    private AjaxButton applyDateRangeButton;

    List<PlannedOrder> plannedOrderList = new ArrayList<>();
    List<ProductionOrder> productionOrderList = new ArrayList<>();
    List<SalesOrder> allSalesOrderList = new ArrayList<>();

    public SalesOrderDynamicTabsPage(String id) {
        super(id);

        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                // Add custom CSS for proper styling
                response.render(OnDomReadyHeaderItem.forScript(
                        "// Custom styling for the table container" +
                                "$('.table-container').css('margin-top', '1rem');"
                ));
            }
        });

        // Loading container and label
        loadingContainer = new WebMarkupContainer("loadingContainer");
        loadingContainer.setOutputMarkupId(true);
        loadingContainer.setVisible(false); // Initially hidden
        add(loadingContainer);

        loadingLabel = new Label("loadingLabel", Model.of(""));
        loadingLabel.setOutputMarkupId(true);
        loadingContainer.add(loadingLabel);

        // Form
        Form<Void> form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);

        // Date Pickers
        startDateFilter = LocalDate.now().minusMonths(1); // Default start date
        endDateFilter = LocalDate.now(); // Default end date

        DatePicker startDatePicker = getStartDatePicker();
        form.add(startDatePicker);

        DatePicker endDatePicker = getEndDatePicker();
        form.add(endDatePicker);

        // Apply Button
        applyDateRangeButton = new AjaxButton("applyDateRangeButton", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
                loadSalesOrderData(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                super.onError(target);
                target.add(feedback);
            }
        };
        form.add(applyDateRangeButton);

        // Feedback Panel
        feedback = new JQueryFeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        // Table Container - initially hidden
        tableContainer = new WebMarkupContainer("tableContainer");
        tableContainer.setOutputMarkupId(true);
        tableContainer.setVisible(false); // Initially hidden
        form.add(tableContainer);
    }

    private DatePicker getStartDatePicker() {
        Options options = new Options();
        options.set("dateFormat", "'dd.mm.yy'");
        options.set("firstDay", 1);
        options.set("changeMonth", true);
        options.set("changeYear", true);

        DatePicker startDatePicker = new DatePicker("startDatePicker",
                new PropertyModel<>(this, "startDateFilter"), "dd.MM.yyyy", options);
        startDatePicker.setRequired(true);
        return startDatePicker;
    }

    private DatePicker getEndDatePicker() {
        Options options = new Options();
        options.set("dateFormat", "'dd.mm.yy'");
        options.set("firstDay", 1);
        options.set("changeMonth", true);
        options.set("changeYear", true);

        DatePicker endDatePicker = new DatePicker("endDatePicker",
                new PropertyModel<>(this, "endDateFilter"), "dd.MM.yyyy", options);
        endDatePicker.setRequired(true);
        return endDatePicker;
    }

    private void loadSalesOrderData(AjaxRequestTarget target) {
        // Show loading indicator
        loadingContainer.setVisible(true);
        loadingLabel.setDefaultModelObject("Loading sales order from SAP ...");
        target.add(loadingContainer, feedback);

        try {
            allSalesOrderList = SalesOrderApiClient.getData(startDateFilter, endDateFilter);
            plannedOrderList = PlannedOrderApiClient.getData(startDateFilter, endDateFilter);
            productionOrderList = ProductionOrderApiClient.getData(startDateFilter, endDateFilter);

            if (!allSalesOrderList.isEmpty()) {
                updateTableWithData(target);
                info("Loaded " + getDistinctWeeks(allSalesOrderList).size() + " delivery weeks");
            } else {
                tableContainer.setVisible(false);
                info("No sales orders found for the selected date range.");
            }
        } catch (Exception e) {
            LOG.error("Error loading sales order data", e);
            error("Failed to load sales order data: " + e.getMessage());
            tableContainer.setVisible(false);
        } finally {
            loadingContainer.setVisible(false);
            target.add(loadingContainer, feedback, tableContainer);
        }
    }

    private void updateTableWithData(AjaxRequestTarget target) {
        // Remove existing panel if present
        if (salesOrderPanel != null) {
            tableContainer.remove(salesOrderPanel);
        }

        // Create new sales order panel with all data and dropdown support
        salesOrderPanel = new SalesOrderPanel("salesOrderPanel",
                allSalesOrderList, plannedOrderList, productionOrderList);

        tableContainer.add(salesOrderPanel);
        tableContainer.setVisible(true);
    }

    private List<String> getDistinctWeeks(List<SalesOrder> salesOrders) {
        return salesOrders.stream()
                .map(SalesOrder::getRequestedDeliveryWeek)
                .distinct()
                .sorted((week1, week2) -> {
                    String[] parts1 = week1.split("/");
                    String[] parts2 = week2.split("/");

                    int weekNum1 = Integer.parseInt(parts1[0]);
                    int year1 = Integer.parseInt(parts1[1]);

                    int weekNum2 = Integer.parseInt(parts2[0]);
                    int year2 = Integer.parseInt(parts2[1]);

                    if (year1 != year2) {
                        return Integer.compare(year1, year2);
                    } else {
                        return Integer.compare(weekNum1, weekNum2);
                    }
                })
                .collect(Collectors.toList());
    }
}