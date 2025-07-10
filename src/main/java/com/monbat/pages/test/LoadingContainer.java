package com.monbat.pages.test;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class LoadingContainer extends Panel {
    private final Label loadingLabel;

    public LoadingContainer(String id) {
        super(id);
        setOutputMarkupPlaceholderTag(true);

        loadingLabel = new Label("loadingLabel", Model.of("Loading..."));
        loadingLabel.setOutputMarkupId(true);
        add(loadingLabel);
    }

    public void setLoadingMessage(String message) {
        loadingLabel.setDefaultModelObject(message);
    }
}
