package com.monbat.pages.newTimeline;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

public class MainTimelinePanel extends Panel {
    public MainTimelinePanel(String id) {
        super(id);

        WebMarkupContainer mainContainer = new WebMarkupContainer("mainContainer");
        mainContainer.add(AttributeModifier.replace("class", "main-timeline-container"));
        add(mainContainer);

        // Add three child panels
        mainContainer.add(new ChildPanel("childPanel1"));
        mainContainer.add(new ChildPanel("childPanel2"));
        mainContainer.add(new ChildPanel("childPanel3"));
    }
}
