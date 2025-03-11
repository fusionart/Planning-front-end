package com.monbat.pages.tabs;

import com.monbat.models.dto.ReadinessByWeek;
import com.monbat.models.dto.ReadinessDetailWithDate;
import com.monbat.models.entities.TabData;
import com.monbat.pages.readinessComponent.ReadinessTable;
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

public class DynamicTabsPage extends Panel {
    private final WebMarkupContainer contentContainer;
    private final ListView<TabData> tabHeaders;
    private final Label loadingLabel;
    private final WebMarkupContainer tabsContainer;
    private String activeTabId; // Track the currently active tab ID
    private final transient RestTemplate restTemplate;

    public DynamicTabsPage(String id) {
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

                        ReadinessTable newTable = new ReadinessTable("content", Model.of(tabData.getContent()));
                        contentContainer.replace(newTable);
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

                // Set the first tab as active by default
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
                List<ReadinessByWeek> readinessData = getReadinessByWeek();

                if (readinessData != null) {
                    loadingLabel.setDefaultModel(Model.of(""));
                    loadingLabel.setVisible(false);

                    List<TabData> tabDataList = createTabDataList(readinessData);
                    tabHeaders.setModelObject(tabDataList);

                    if (!tabDataList.isEmpty()) {
                        ReadinessTable newTable = new ReadinessTable("content",
                                Model.of(tabDataList.get(0).getContent()));
                        contentContainer.replace(newTable);
                        contentContainer.setVisible(true);
                    }

                    stop(target);
                    target.add(loadingLabel, contentContainer, tabsContainer);
                }
            }
        });
    }

    private void updateTabStates(AjaxRequestTarget target) {
        target.add(tabsContainer);
    }

    private List<TabData> createTabDataList(List<ReadinessByWeek> readinessData) {
        List<TabData> tabDataList = new ArrayList<>();

        for (ReadinessByWeek item : readinessData) {
            for (Map.Entry<String, List<ReadinessDetailWithDate>> readinessByDateEntry : item.getMap().entrySet()) {
//                List<ReadinessDetail> tableData = new ArrayList<>();
//                for (ReadinessByDate readinessByDate : readinessByDateEntry.getValue()) {
//
//                    for (Map.Entry<Date, List<ReadinessDetail>> readinessDetail : readinessByDate.getMap().entrySet()) {
//                        tableData.addAll(readinessDetail.getValue());
//                    }
//                }
                tabDataList.add(new TabData("Week " + readinessByDateEntry.getKey(),
                        readinessByDateEntry.getValue()));
            }
        }
        return tabDataList;
    }

    private List<ReadinessByWeek> getReadinessByWeek() {
        String apiUrl = "http://localhost:8080/api/calculations/plan10s";

        try {
            ResponseEntity<List<ReadinessByWeek>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            // Log the error and return an empty list
            LoggerFactory.getLogger(getClass()).error("Error fetching readiness data", e);
            return Collections.emptyList();
        }
    }
}