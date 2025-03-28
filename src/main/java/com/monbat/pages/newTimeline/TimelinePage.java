package com.monbat.pages.newTimeline;

import org.apache.wicket.markup.html.panel.Panel;

public class TimelinePage extends Panel {
    public TimelinePage(String id) {
        super(id);

        // Add Timeline Header
        add(new TimelineHeaderPanel("timelineHeader"));

        // Add Main Timeline Panel
        add(new MainTimelinePanel("mainTimelinePanel"));
    }
}
