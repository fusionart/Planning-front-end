package com.monbat.pages.newTimeline;

import com.monbat.models.entities.TimelineRectangle;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChildPanel extends Panel {
    private static final int HOUR_WIDTH = 50    ;
    private static final int VERTICAL_ROWS = 10;
    private static final int ROW_HEIGHT = 40;
    private static final int TIMELINE_LEFT_OFFSET = 200;

    private static final String[] COLORS = {
            "#3498db", "#2ecc71", "#e74c3c",
            "#f39c12", "#9b59b6", "#1abc9c"
    };

    public ChildPanel(String id) {
        super(id);

        // Create timeline header
        ListView<LocalTime> timelineHeader = new ListView<>("hoursList",
                IntStream.range(0, 24)
                        .mapToObj(hour -> LocalTime.of(hour, 0))
                        .collect(Collectors.toList())) {
            @Override
            protected void populateItem(ListItem<LocalTime> item) {
                LocalTime hour = item.getModelObject();
                Label hourLabel = new Label("hourLabel",
                        hour.format(DateTimeFormatter.ofPattern("HH")));

                String style = String.format(
                        "left: %dpx; width: %dpx;",  // Use %d instead of %.2f for integers
                        TIMELINE_LEFT_OFFSET + (item.getIndex() * (int)HOUR_WIDTH),
                        (int)HOUR_WIDTH
                );

                item.add(AttributeModifier.replace("style", style));
                item.add(hourLabel);
            }
        };
        add(timelineHeader);

        // Create rectangles
        List<TimelineRectangle> rectangles = generateSampleRectangles();
        List<Integer> verticalPositions = calculateVerticalPositions(rectangles);

        ListView<TimelineRectangle> rectangleListView = new ListView<>("rectangleList", rectangles) {
            @Override
            protected void populateItem(ListItem<TimelineRectangle> item) {
                TimelineRectangle rectangle = item.getModelObject();
                int verticalPosition = verticalPositions.get(item.getIndex());

                WebMarkupContainer rectangleDiv = new WebMarkupContainer("rectangleItem");
                String color = rectangle.getColor() != null ? rectangle.getColor() :
                        COLORS[verticalPosition % COLORS.length];

                double leftPos = TIMELINE_LEFT_OFFSET + (calculateTimeOffset(rectangle.getStartDateTime()) * HOUR_WIDTH);
                double width = (ChronoUnit.MINUTES.between(
                        rectangle.getStartDateTime(),
                        rectangle.getEndDateTime()
                ) / 60.0) * HOUR_WIDTH;

                int topPos = verticalPosition * ROW_HEIGHT;

                String style = String.format(
                        "position: absolute; " +
                                "left: %dpx; " +  // Changed from %.2f to %d
                                "width: %dpx; " + // Changed from %.2f to %d
                                "height: %dpx; " +
                                "top: %dpx; " +
                                "background-color: %s; " +
                                "color: white; " +
                                "display: flex; " +
                                "align-items: center; " +
                                "justify-content: center; " +
                                "border-radius: 4px; " +
                                "box-shadow: 0 1px 3px rgba(0,0,0,0.2); " +
                                "z-index: %d;",
                        (int)leftPos,
                        (int)width,
                        ROW_HEIGHT - 5,
                        topPos,
                        color,
                        verticalPosition + 10
                );

                rectangleDiv.add(AttributeModifier.replace("style", style));
                rectangleDiv.add(AttributeModifier.append("class", "timeline-rectangle"));

                Label titleLabel = new Label("rectangleTitle", rectangle.getTitle());
                rectangleDiv.add(titleLabel);
                item.add(rectangleDiv);
            }
        };
        add(rectangleListView);
    }

    private List<Integer> calculateVerticalPositions(List<TimelineRectangle> rectangles) {
        List<Integer> verticalPositions = new ArrayList<>();
        List<Double> endTimes = new ArrayList<>();

        for (TimelineRectangle rectangle : rectangles) {
            double startTime = calculateTimeOffset(rectangle.getStartDateTime());
            int verticalLevel = 0;

            while (verticalLevel < endTimes.size() && startTime < endTimes.get(verticalLevel)) {
                verticalLevel++;
            }

            double endTime = calculateTimeOffset(rectangle.getEndDateTime());
            if (verticalLevel < endTimes.size()) {
                endTimes.set(verticalLevel, endTime);
            } else {
                endTimes.add(endTime);
            }

            verticalPositions.add(verticalLevel % VERTICAL_ROWS); // Ensure we stay within 10 rows
        }

        return verticalPositions;
    }

    private int calculateTimeOffset(LocalDateTime dateTime) {
        LocalDateTime timelineStart = LocalDateTime.now().minusDays(3).withHour(0).withMinute(0).withSecond(0);
        return (int)(ChronoUnit.MINUTES.between(timelineStart, dateTime) / 60.0);
    }

    private List<TimelineRectangle> generateSampleRectangles() {
        List<TimelineRectangle> rectangles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        rectangles.add(new TimelineRectangle(
                now.plusHours(2), now.plusHours(4), "Meeting", COLORS[0]));
        rectangles.add(new TimelineRectangle(
                now.plusHours(3), now.plusHours(5), "Discussion", COLORS[1]));
        rectangles.add(new TimelineRectangle(
                now.minusHours(1), now.plusHours(1), "Work Block", COLORS[2]));
        rectangles.add(new TimelineRectangle(
                now.plusDays(1).plusHours(3), now.plusDays(1).plusHours(5), "Project Review", COLORS[3]));
        rectangles.add(new TimelineRectangle(
                now.plusHours(5), now.plusHours(7), "Training", COLORS[4]));

        return rectangles;
    }
}
