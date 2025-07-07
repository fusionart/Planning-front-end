package com.monbat.pages.salesOrders;

import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.form.datepicker.DatePicker;
import com.googlecode.wicket.jquery.ui.panel.JQueryFeedbackPanel;
import com.googlecode.wicket.jquery.ui.widget.tabs.TabListModel;
import com.googlecode.wicket.jquery.ui.widget.tabs.TabbedPanel;
import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import com.monbat.models.entities.TabData;
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
import java.util.Collections;
import java.util.List;

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
        options.set("dateFormat", "'dd.mm.yy'"); // Note the quotes around the format
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
        options.set("dateFormat", "'dd.mm.yy'"); // Note the quotes around the format
        options.set("firstDay", 1);
        options.set("changeMonth", true);
        options.set("changeYear", true);

        DatePicker endDatePicker = new DatePicker("endDatePicker",
                new PropertyModel<>(this, "endDateFilter"), "dd.MM.yyyy", options);
        endDatePicker.setRequired(true);
        return endDatePicker;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }


    private void loadSalesOrderData(AjaxRequestTarget target) {
        loadingContainer.setVisible(true); // Show loading indicator
        loadingLabel.setDefaultModelObject("Loading sales order from SAP ...");
        target.add(loadingContainer, feedback); // Update loading status immediately

        try {
            List<SalesOrder> salesOrderList = SalesOrderApiClient.getData(startDateFilter, endDateFilter);
            plannedOrderList = PlannedOrderApiClient.getData(startDateFilter, endDateFilter);
            productionOrderList = ProductionOrderApiClient.getData(startDateFilter, endDateFilter);

            if (!salesOrderList.isEmpty()) {
                loadingContainer.setVisible(false);
                updateTabsWithData(salesOrderList, target); // This method now correctly updates the form which contains the new tabbed panel
            } else {
                loadingContainer.setVisible(false);
                tabbedPanel.setVisible(false); // Hide tabs if no data
                info("No sales orders found for the selected date range.");
                target.add(tabbedPanel, feedback); // Add tabbedPanel and feedback to target if no data
            }
        } catch (Exception e) {
            LOG.error("Error loading sales order data", e);
            error("Failed to load sales order data: " + e.getMessage());
            target.add(feedback);
        } finally {
            loadingContainer.setVisible(false);
            target.add(loadingContainer); // Always ensure loading container is hidden
        }
        // Removed the problematic target.add(tabbedPanel, loadingContainer, feedback); line here.
        // The updateTabsWithData method already handles adding the 'form' to the target,
        // which now contains the newly created and visible tabbedPanel.
    }

    private void updateTabsWithData(List<SalesOrder> allItems, AjaxRequestTarget target) {
        List<String> distinctWeeks = getDistinctWeeks(allItems);

        List<TabData<List<SalesOrder>>> tabDataList = new ArrayList<>();
        for (String week : distinctWeeks){
            List<SalesOrder> filteredSalesOrders = allItems.stream()
                    .filter(order -> week.equals(order.getRequestedDeliveryWeek()))
                    .toList();
            loadingLabel = new Label("loadingLabel", Model.of(week));
            tabDataList.add(new TabData<>("Week " + week, Collections.singletonList(filteredSalesOrders)));
        }

        // Create new tabs and update the model
        List<ITab> tabs = createTabsFromData(tabDataList);

        // Replace the existing tabbedPanel with a new one
        Form<?> form = (Form<?>) tabbedPanel.getParent();
        form.remove(tabbedPanel);

        Options options = new Options();
        options.set("collapsible", false);
        options.set("active", 0);

        tabbedPanel = new TabbedPanel("tabs", tabs, options) {

            @Override
            public void onActivate(AjaxRequestTarget target, int index, ITab tab) {
                info("Selected tab # " + index + ": " + tab.getTitle());
                target.add(feedback);
            }
        };

        tabbedPanel.setOutputMarkupId(true);
        tabbedPanel.setVisible(true); // Ensure the new panel is visible
        form.add(tabbedPanel); // Add the new panel to the form

        info("Loaded " + tabs.size() + " delivery weeks");
        target.add(form); // Update the entire form to ensure the new tabbedPanel is rendered
    }

    private List<ITab> createTabsFromData(List<TabData<List<SalesOrder>>> tabDataList) {
        List<ITab> tabs = new ArrayList<>();

        for (TabData<List<SalesOrder>> tabData : tabDataList) {
            final List<SalesOrder> contentModel = tabData.getContent().get(0);

            tabs.add(new AbstractTab(Model.of(tabData.getTitle())) {

                @Override
                public WebMarkupContainer getPanel(String panelId) {
                    return new SalesOrderPanel(panelId, contentModel, plannedOrderList, productionOrderList);
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
                    // Parse week and year from the strings
                    String[] parts1 = week1.split("/");
                    String[] parts2 = week2.split("/");

                    int weekNum1 = Integer.parseInt(parts1[0]);
                    int year1 = Integer.parseInt(parts1[1]);

                    int weekNum2 = Integer.parseInt(parts2[0]);
                    int year2 = Integer.parseInt(parts2[1]);

                    // First compare by year, then by week number
                    if (year1 != year2) {
                        return Integer.compare(year1, year2);
                    } else {
                        return Integer.compare(weekNum1, weekNum2);
                    }
                })
                .toList();
    }
}