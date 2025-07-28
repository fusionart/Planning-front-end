package com.monbat.pages.salesOrders;

import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.form.datepicker.DatePicker;
import com.googlecode.wicket.jquery.ui.panel.JQueryFeedbackPanel;
import com.googlecode.wicket.jquery.ui.widget.tabs.TabListModel;
import com.googlecode.wicket.jquery.ui.widget.tabs.TabbedPanel;
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
    private TabbedPanel tabbedPanel;
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
                response.render(OnDomReadyHeaderItem.forScript(
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
        add(loadingContainer);

        loadingLabel = new Label("loadingLabel", Model.of("Loading sales order from SAP ..."));
        loadingLabel.setOutputMarkupId(true);
        loadingContainer.add(loadingLabel);

        // Form with inline date range controls
        Form<Void> form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);

        // Date Pickers with default values
        startDateFilter = LocalDate.now().minusMonths(1);
        endDateFilter = LocalDate.now();

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

        // Initialize TabbedPanel with empty model
        Options options = new Options();
        options.set("collapsible", false);
        options.set("active", 0);

        tabbedPanel = new TabbedPanel("tabs", new TabListModel() {
            @Override
            protected List<ITab> load() {
                return new ArrayList<>();
            }
        }, options) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onActivate(AjaxRequestTarget target, int index, ITab tab) {
                info("Selected tab #" + index + ": " + tab.getTitle());
                target.add(feedback);
            }
        };

        tabbedPanel.setOutputMarkupId(true);
        tabbedPanel.setVisible(false); // Initially hidden
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
        loadingContainer.setVisible(true);
        loadingLabel.setDefaultModelObject("Loading sales order from SAP ...");
        target.add(loadingContainer, feedback);

        try {
            List<SalesOrder> salesOrderList = SalesOrderApiClient.getData(startDateFilter, endDateFilter);
            plannedOrderList = PlannedOrderApiClient.getData(startDateFilter, endDateFilter);
            productionOrderList = ProductionOrderApiClient.getData(startDateFilter, endDateFilter);

            if (!salesOrderList.isEmpty()) {
                loadingContainer.setVisible(false);
                updateTabsWithData(salesOrderList, target);
            } else {
                loadingContainer.setVisible(false);
                tabbedPanel.setVisible(false);
                info("No sales orders found for the selected date range.");
                target.add(tabbedPanel, feedback);
            }
        } catch (Exception e) {
            LOG.error("Error loading sales order data", e);
            error("Failed to load sales order data: " + e.getMessage());
            target.add(feedback);
        } finally {
            loadingContainer.setVisible(false);
            target.add(loadingContainer);
        }
    }

    private void updateTabsWithData(List<SalesOrder> allItems, AjaxRequestTarget target) {
        List<String> distinctWeeks = getDistinctWeeks(allItems);

        // Create week-based tabs
        List<ITab> tabs = new ArrayList<>();

        for (String week : distinctWeeks) {
            List<SalesOrder> filteredSalesOrders = allItems.stream()
                    .filter(order -> week.equals(order.getRequestedDeliveryWeek()))
                    .collect(Collectors.toList());

            tabs.add(new AbstractTab(Model.of("Week " + week)) {
                private static final long serialVersionUID = 1L;

                @Override
                public WebMarkupContainer getPanel(String panelId) {
                    // Use the new WeekSalesOrderPanel
                    return new WeekSalesOrderPanel(panelId, filteredSalesOrders, plannedOrderList, productionOrderList);
                }
            });
        }

        // Replace the existing tabbedPanel with a new one
        Form<?> form = (Form<?>) tabbedPanel.getParent();
        form.remove(tabbedPanel);

        Options options = new Options();
        options.set("collapsible", false);
        options.set("active", 0);

        tabbedPanel = new TabbedPanel("tabs", tabs, options) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onActivate(AjaxRequestTarget target, int index, ITab tab) {
                info("Selected tab # " + index + ": " + tab.getTitle());
                target.add(feedback);
            }
        };

        tabbedPanel.setOutputMarkupId(true);
        tabbedPanel.setVisible(true);
        form.add(tabbedPanel);

        info("Loaded " + tabs.size() + " delivery weeks");
        target.add(form);
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