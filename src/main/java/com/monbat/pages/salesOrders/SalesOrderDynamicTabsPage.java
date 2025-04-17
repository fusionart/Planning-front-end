package com.monbat.pages.salesOrders;

import com.monbat.models.dto.sap.SalesOrderDto;
import com.monbat.models.dto.sap.SalesOrderItemRow;
import com.monbat.models.dto.sap.ToItem;
import com.monbat.models.entities.TabData;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.monbat.utils.Constants.MAIN_ADDRESS;

public class SalesOrderDynamicTabsPage extends Panel {
    private final WebMarkupContainer contentContainer;
    private final ListView<TabData> tabHeaders;
    private final Label loadingLabel;
    private final WebMarkupContainer tabsContainer;
    private String activeTabId;
    private final transient RestTemplate restTemplate;

    public SalesOrderDynamicTabsPage(String id) {
        super(id);
        restTemplate = new RestTemplate();

        loadingLabel = new Label("loadingLabel", Model.of("Loading data..."));
        loadingLabel.setOutputMarkupId(true);
        add(loadingLabel);

        contentContainer = new WebMarkupContainer("contentContainer");
        contentContainer.setOutputMarkupId(true);
        contentContainer.setOutputMarkupPlaceholderTag(true);
        contentContainer.setVisible(false);
        add(contentContainer);
        contentContainer.add(new Label("content", Model.of("")));

        tabsContainer = new WebMarkupContainer("tabsContainer");
        tabsContainer.setOutputMarkupId(true);
        add(tabsContainer);

        tabHeaders = new ListView<>("tabHeaders", new ArrayList<>()) {
            @Override
            protected void populateItem(ListItem<TabData> item) {
                TabData tabData = item.getModelObject();
                if (tabData == null) {
                    throw new IllegalArgumentException("TabData object must not be null.");
                }

                String tabId = "tab_" + item.getIndex();
                AjaxLink<Void> tabLink = new AjaxLink<>("tabLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        activeTabId = tabId;
                        updateTabStates(target);

                        SalesOrderPanel newPanel = new SalesOrderPanel("content", tabData.getContent());
                        contentContainer.replace(newPanel);
                        target.add(contentContainer);
                    }

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        String cssClass = tabId.equals(activeTabId) ? "tab-link active" : "tab-link";
                        add(AttributeModifier.replace("class", cssClass));
                    }
                };

                tabLink.setOutputMarkupId(true);
                tabLink.add(new Label("tabTitle", tabData.getTitle()));
                item.add(tabLink);

                if (item.getIndex() == 0 && activeTabId == null) {
                    activeTabId = tabId;
                }
            }
        };

        tabHeaders.setOutputMarkupId(true);
        tabHeaders.setModel(new ListModel<>(new ArrayList<>()));
        tabsContainer.add(tabHeaders);

        add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                List<SalesOrderItemRow> allItems = getFlattenedModel(fetchSalesOrders());

                if (!allItems.isEmpty()) {
                    loadingLabel.setDefaultModel(Model.of(""));
                    loadingLabel.setVisible(false);

                    // Group items by week
                    Map<String, List<SalesOrderItemRow>> itemsByWeek = allItems.stream()
                            .collect(Collectors.groupingBy(SalesOrderItemRow::getRequestedDeliveryWeek));

                    // Create tab data for each week
                    List<TabData> tabDataList = new ArrayList<>();
                    itemsByWeek.forEach((week, items) -> {
                        tabDataList.add(new TabData<>("Week " + week, items));
                    });

                    // Sort tabs by week number (numeric value before the slash)
                    tabDataList.sort((t1, t2) -> {
                        // Extract week numbers (part before the slash)
                        int week1 = Integer.parseInt(t1.getTitle().split(" ")[1].split("/")[0]);
                        int week2 = Integer.parseInt(t2.getTitle().split(" ")[1].split("/")[0]);
                        return Integer.compare(week1, week2);
                    });

                    tabHeaders.setModelObject(tabDataList);

                    if (!tabDataList.isEmpty()) {
                        SalesOrderPanel newPanel = new SalesOrderPanel("content", tabDataList.get(0).getContent());
                        contentContainer.replace(newPanel);
                        contentContainer.setVisible(true);
                    }

                    stop(target);
                    target.add(loadingLabel, contentContainer, tabsContainer);
                }
            }
        });
    }

    private List<SalesOrderDto> fetchSalesOrders() {
        String apiUrl = MAIN_ADDRESS + "api/sap/getSalesOrders";
        try {
            ResponseEntity<List<SalesOrderDto>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody();
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("Error fetching sales orders data", e);
            return Collections.emptyList();
        }
    }

    private List<SalesOrderItemRow> getFlattenedModel(List<SalesOrderDto> salesOrders){
        List<SalesOrderItemRow> rows = new ArrayList<>();
        for (SalesOrderDto order : salesOrders) {
            if (order.getToItem() == null || order.getToItem().isEmpty()) {
                // Add a row for orders with no items
                rows.add(new SalesOrderItemRow(order, null));
            } else {
                // Add a row for each item in the order
                for (ToItem item : order.getToItem()) {
                    if (StringUtils.left(item.getMaterial(),2).equals("10")){
                        rows.add(new SalesOrderItemRow(order, item));
                    }
                }
            }
        }
        return rows;
    };


    private void updateTabStates(AjaxRequestTarget target) {
        target.add(tabsContainer);
    }
}
