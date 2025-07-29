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
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
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
    private TabbedPanel<ITab> tabbedPanel;
    private FeedbackPanel feedback;

    private LocalDate startDateFilter;
    private LocalDate endDateFilter;
    private AjaxButton applyDateRangeButton;

    List<PlannedOrder> plannedOrderList = new ArrayList<>();
    List<ProductionOrder> productionOrderList = new ArrayList<>();

    public SalesOrderDynamicTabsPage(String id) {
        super(id);

        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                // Add custom CSS for proper tab styling and prevent clicks
                response.render(OnDomReadyHeaderItem.forScript(
                        "// Ensure tabs are properly styled and prevent navigation" +
                                "$('.tabpanel').addClass('nav nav-tabs');" +
                                "$('.tabpanel li').addClass('nav-item');" +
                                "$('.tabpanel li a').addClass('nav-link');" +
                                "$('.tabpanel li.selected a').addClass('active');" +

                                // Prevent tab clicks from navigating until data is loaded
                                "$('.tabpanel li a').on('click', function(e) {" +
                                "   if (!$(this).hasClass('data-loaded')) {" +
                                "       e.preventDefault();" +
                                "       e.stopPropagation();" +
                                "       return false;" +
                                "   }" +
                                "});" +

                                "const problematicTables = [];" +
                                "document.querySelectorAll('.table').forEach(table => {" +
                                "   if(table.offsetWidth === 0 || table.offsetHeight === 0) {" +
                                "       problematicTables.push(table.id);" +
                                "   }" +
                                "});" +
                                "console.log('Hidden tables:', problematicTables);"
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

        // Initialize TabbedPanel with empty tabs - initially hidden
        tabbedPanel = new TabbedPanel<ITab>("tabs", new ArrayList<>()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected String getTabContainerCssClass() {
                return "nav nav-tabs";
            }

            @Override
            protected String getSelectedTabCssClass() {
                return "nav-link active";
            }

            @Override
            protected String getLastTabCssClass() {
                return "nav-link";
            }
        };

        tabbedPanel.setOutputMarkupId(true);
        tabbedPanel.setVisible(false); // Initially hidden - only show after Apply is clicked
        form.add(tabbedPanel);
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
            List<SalesOrder> salesOrderList = SalesOrderApiClient.getData(startDateFilter, endDateFilter);
            plannedOrderList = PlannedOrderApiClient.getData(startDateFilter, endDateFilter);
            productionOrderList = ProductionOrderApiClient.getData(startDateFilter, endDateFilter);

            if (!salesOrderList.isEmpty()) {
                updateTabsWithData(salesOrderList, target);
                info("Loaded " + getDistinctWeeks(salesOrderList).size() + " delivery weeks");
            } else {
                tabbedPanel.setVisible(false);
                info("No sales orders found for the selected date range.");
            }
        } catch (Exception e) {
            LOG.error("Error loading sales order data", e);
            error("Failed to load sales order data: " + e.getMessage());
            tabbedPanel.setVisible(false);
        } finally {
            loadingContainer.setVisible(false);
            target.add(loadingContainer, feedback, tabbedPanel);
        }
    }

    private void updateTabsWithData(List<SalesOrder> allItems, AjaxRequestTarget target) {
        List<String> distinctWeeks = getDistinctWeeks(allItems);

        // Create new tabs
        List<ITab> tabs = createTabsFromWeeks(distinctWeeks, allItems);

        // Replace the existing tabbedPanel with a new one
        Form<?> form = (Form<?>) tabbedPanel.getParent();
        form.remove(tabbedPanel);

        // Create new TabbedPanel with data
        tabbedPanel = new TabbedPanel<ITab>("tabs", tabs) {
            private static final long serialVersionUID = 1L;

            @Override
            protected String getTabContainerCssClass() {
                return "nav nav-tabs";
            }

            @Override
            protected String getSelectedTabCssClass() {
                return "nav-link active data-loaded";
            }

            @Override
            protected String getLastTabCssClass() {
                return "nav-link data-loaded";
            }

            @Override
            protected WebMarkupContainer newTabsContainer(final String id) {
                WebMarkupContainer container = super.newTabsContainer(id);
                container.add(new org.apache.wicket.AttributeModifier("class", "nav nav-tabs"));
                return container;
            }
        };

        tabbedPanel.setOutputMarkupId(true);
        tabbedPanel.setVisible(true); // Show tabs only after data is loaded
        form.add(tabbedPanel);
    }

    private List<ITab> createTabsFromWeeks(List<String> distinctWeeks, List<SalesOrder> allItems) {
        List<ITab> tabs = new ArrayList<>();

        for (String week : distinctWeeks) {
            final List<SalesOrder> filteredSalesOrders = allItems.stream()
                    .filter(order -> week.equals(order.getRequestedDeliveryWeek()))
                    .collect(Collectors.toList());

            tabs.add(new AbstractTab(Model.of("Week " + week)) {
                private static final long serialVersionUID = 1L;

                @Override
                public WebMarkupContainer getPanel(String panelId) {
                    return new SalesOrderPanel(panelId, filteredSalesOrders, plannedOrderList, productionOrderList);
                }
            });
        }

        return tabs;
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