package com.monbat.pages.salesOrders;

import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.ui.panel.JQueryFeedbackPanel;
import com.googlecode.wicket.jquery.ui.widget.tabs.TabListModel;
import com.googlecode.wicket.jquery.ui.widget.tabs.TabbedPanel;
import com.monbat.models.dto.sap.SalesOrderDto;
import com.monbat.models.dto.sap.SalesOrderItemRow;
import com.monbat.models.dto.sap.ToItem;
import com.monbat.models.entities.TabData;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.monbat.utils.Constants.MAIN_ADDRESS;

public class SalesOrderDynamicTabsPage extends Panel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SalesOrderDynamicTabsPage.class);

    private final WebMarkupContainer loadingContainer;
    private final Label loadingLabel;
    private TabbedPanel tabbedPanel;
    private FeedbackPanel feedback;
    private final transient RestTemplate restTemplate;

    public SalesOrderDynamicTabsPage(String id) {
        super(id);
        restTemplate = new RestTemplate();

        // Loading container and label
        loadingContainer = new WebMarkupContainer("loadingContainer");
        loadingContainer.setOutputMarkupId(true);
        add(loadingContainer);

        loadingLabel = new Label("loadingLabel", Model.of("Loading sales order data..."));
        loadingLabel.setOutputMarkupId(true);
        loadingContainer.add(loadingLabel);

        // Form
        Form<Void> form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);

        // Feedback Panel
        feedback = new JQueryFeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        // Initialize TabbedPanel with empty model
        Options options = new Options();
        options.set("collapsible", false);
        options.set("active", 0);

        tabbedPanel = new TabbedPanel("tabs", new TabListModel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<ITab> load() {
                // Initially return empty list of tabs
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
        tabbedPanel.setVisible(false);
        form.add(tabbedPanel);

        // Add timer behavior to load data
        add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                try {
                    List<SalesOrderItemRow> allItems = getFlattenedModel(fetchSalesOrders());

                    if (!allItems.isEmpty()) {
                        loadingContainer.setVisible(false);
                        updateTabsWithData(allItems, target);
                        stop(target);
                    }
                } catch (Exception e) {
                    LOG.error("Error loading sales order data", e);
                    error("Failed to load sales order data: " + e.getMessage());
                    stop(target);
                }

                target.add(loadingContainer, form, feedback);
            }
        });
    }

    /**
     * Updates the tabbed panel with the fetched sales order data
     */
    private void updateTabsWithData(List<SalesOrderItemRow> allItems, AjaxRequestTarget target) {
        // Group items by week
        Map<String, List<SalesOrderItemRow>> itemsByWeek = allItems.stream()
                .collect(Collectors.groupingBy(SalesOrderItemRow::getRequestedDeliveryWeek));

        // Create tab data list
        List<TabData<List<SalesOrderItemRow>>> tabDataList = new ArrayList<>();
        itemsByWeek.forEach((week, items) -> {
            tabDataList.add(new TabData<>("Week " + week, Collections.singletonList(items)));
        });

        // Sort tabs by week number
        tabDataList.sort((t1, t2) -> {
            try {
                int week1 = Integer.parseInt(t1.getTitle().split(" ")[1].split("/")[0]);
                int week2 = Integer.parseInt(t2.getTitle().split(" ")[1].split("/")[0]);
                return Integer.compare(week1, week2);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOG.warn("Error parsing week numbers for sorting", e);
                return t1.getTitle().compareTo(t2.getTitle());
            }
        });

        // Create new tabs and update the model
        List<ITab> tabs = createTabsFromData(tabDataList);

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
                info("Selected tab #" + index + ": " + tab.getTitle());
                target.add(feedback);
            }
        };

        tabbedPanel.setOutputMarkupId(true);
        tabbedPanel.setVisible(true);
        form.add(tabbedPanel);

        info("Loaded " + tabs.size() + " delivery weeks");
    }

    /**
     * Creates ITab objects from tab data
     */
    private List<ITab> createTabsFromData(List<TabData<List<SalesOrderItemRow>>> tabDataList) {
        List<ITab> tabs = new ArrayList<>();

        for (TabData<List<SalesOrderItemRow>> tabData : tabDataList) {
            // Create a model that will be used to create the panel
            final Model<Serializable> contentModel = Model.of();

            // Use AbstractTab instead of SimpleTab to properly override getPanel
            tabs.add(new org.apache.wicket.extensions.markup.html.tabs.AbstractTab(Model.of(tabData.getTitle())) {
                private static final long serialVersionUID = 1L;

                @Override
                public org.apache.wicket.markup.html.WebMarkupContainer getPanel(String panelId) {
                    return new SalesOrderPanel(panelId, (List<SalesOrderItemRow>) contentModel.getObject());
                }
            });
        }

        return tabs;
    }

    /**
     * Fetches sales orders from the API
     */
    private List<SalesOrderDto> fetchSalesOrders() {
        String apiUrl = MAIN_ADDRESS + "api/sap/getSalesOrders";
        try {
            ResponseEntity<List<SalesOrderDto>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            LOG.error("Error fetching sales orders data", e);
            return Collections.emptyList();
        }
    }

    /**
     * Converts SalesOrderDto list to flattened SalesOrderItemRow list
     */
    private List<SalesOrderItemRow> getFlattenedModel(List<SalesOrderDto> salesOrders) {
        List<SalesOrderItemRow> rows = new ArrayList<>();
        for (SalesOrderDto order : salesOrders) {
            if (order.getToItem() == null || order.getToItem().isEmpty()) {
                // Add a row for orders with no items
                rows.add(new SalesOrderItemRow(order, null));
            } else {
                // Add a row for each item in the order
                for (ToItem item : order.getToItem()) {
                    if (StringUtils.left(item.getMaterial(), 2).equals("10")) {
                        rows.add(new SalesOrderItemRow(order, item));
                    }
                }
            }
        }
        return rows;
    }
}