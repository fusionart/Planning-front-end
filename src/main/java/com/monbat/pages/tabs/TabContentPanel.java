package com.monbat.pages.tabs;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class TabContentPanel extends Panel {
    public TabContentPanel(String id, IModel<String> tabTitleModel, IModel<String> tabContentModel) {
        super(id);

        // Add the tab title
        add(new Label("tabTitle", tabTitleModel));

        // Add the tab content
        add(new Label("tabContent", tabContentModel));

        // You can add more components here based on your requirements
    }
}
