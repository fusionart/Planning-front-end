package com.monbat.pages.newTimeline;

import com.monbat.models.entities.TimelineRectangle;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChildPanel extends Panel {
    private static final double HOUR_WIDTH = 50.0; // Width of each hour in pixels
    private static final int VERTICAL_SPACING = 60; // Space between vertical levels
    private static final int RECTANGLE_HEIGHT = 50; // Height of each rectangle
    private static final int VERTICAL_ROWS = 5;

    private static final String[] COLORS = {
            "#3498db", // Blue
            "#2ecc71", // Green
            "#e74c3c", // Red
            "#f39c12", // Orange
            "#9b59b6", // Purple
            "#1abc9c"  // Turquoise
    };

    public ChildPanel(String id) {
        super(id);

        List<TimelineRectangle> rectangles = generateSampleRectangles();
        List<Integer> verticalPositions = calculateVerticalPositions(rectangles);

        WebMarkupContainer rectanglesContainer = new WebMarkupContainer("rectanglesContainer");
        rectanglesContainer.setOutputMarkupId(true);
        rectanglesContainer.add(AttributeModifier.replace("class", "rectangles-container grid-container"));
        add(rectanglesContainer);

        // Create grid structure
        WebMarkupContainer gridContainer = createGridContainer(rectangles);
        rectanglesContainer.add(gridContainer);

        ListView<TimelineRectangle> rectangleListView = new ListView<>("rectangleList", rectangles) {
            @Override
            protected void populateItem(ListItem<TimelineRectangle> item) {
                TimelineRectangle rectangle = item.getModelObject();
                int verticalPosition = verticalPositions.get(item.getIndex());

                WebMarkupContainer rectangleDiv = new WebMarkupContainer("rectangleItem");

                // Force color if not set
                String color = rectangle.getColor() != null ? rectangle.getColor() : COLORS[verticalPosition % COLORS.length];

                // Calculate dimensions
                double leftPos = calculateTimeOffset(rectangle.getStartDateTime()) * HOUR_WIDTH;
                double width = (ChronoUnit.MINUTES.between(
                        rectangle.getStartDateTime(),
                        rectangle.getEndDateTime()
                ) / 60.0) * HOUR_WIDTH;

                int topPos = verticalPosition * (RECTANGLE_HEIGHT + 10);

                // Create absolute style string
                String style = String.format(
                        "position: absolute !important;" +
                                "left: %.2fpx !important;" +
                                "width: %.2fpx !important;" +
                                "height: %dpx !important;" +
                                "top: %dpx !important;" +
                                "background-color: %s !important;" +
                                "color: white !important;" +
                                "display: flex !important;" +
                                "align-items: center !important;" +
                                "justify-content: center !important;" +
                                "border-radius: 4px !important;" +
                                "box-shadow: 0 1px 3px rgba(0,0,0,0.2) !important;" +
                                "z-index: %d !important;",
                        leftPos, width, RECTANGLE_HEIGHT, topPos, color, verticalPosition + 10
                );

                rectangleDiv.add(AttributeModifier.replace("style", style));
                rectangleDiv.add(AttributeModifier.append("class", "timeline-rectangle grid-rectangle"));

                Label titleLabel = new Label("rectangleTitle", rectangle.getTitle());
                rectangleDiv.add(titleLabel);
                item.add(rectangleDiv);
            }
        };
        rectanglesContainer.add(rectangleListView);
    }

    private WebMarkupContainer createGridContainer(List<TimelineRectangle> rectangles) {
        WebMarkupContainer gridContainer = new WebMarkupContainer("gridContainer");
        gridContainer.setOutputMarkupId(true);

        // Generate hours (24 hours)
        List<Integer> hours = IntStream.range(0, 24).boxed().collect(Collectors.toList());

        // Create horizontal hours header container
        ListView<Integer> headerHoursListView = new ListView<>("hoursList", hours) {
            @Override
            protected void populateItem(ListItem<Integer> item) {
                Integer hour = item.getModelObject();
                Label hourLabel = new Label("hourLabel", String.format("%02d", hour));

                // Set width for each hour column with precise alignment
                hourLabel.add(new AttributeModifier("style",
                        Model.of(String.format("min-width: %.2fpx; width: %.2fpx; text-align: center;", HOUR_WIDTH, HOUR_WIDTH))));

                item.add(hourLabel);
            }
        };
        gridContainer.add(headerHoursListView);

        // Create grid rows with TimelineRectangles
        ListView<TimelineRectangle> rowsListView = new ListView<>("rowsList",
                rectangles.subList(0, Math.min(VERTICAL_ROWS, rectangles.size()))) {
            @Override
            protected void populateItem(ListItem<TimelineRectangle> item) {
                // Create hours list for each row
                ListView<Integer> rowHoursListView = new ListView<>("hoursList", hours) {
                    @Override
                    protected void populateItem(ListItem<Integer> hourItem) {
                        WebMarkupContainer hourCell = new WebMarkupContainer("hourCell");
                        // Optional: Add styling to ensure precise alignment
                        hourCell.add(new AttributeModifier("style",
                                Model.of(String.format("min-width: %.2fpx; width: %.2fpx;", HOUR_WIDTH, HOUR_WIDTH))));
                        hourItem.add(hourCell);
                    }
                };
                item.add(rowHoursListView);
            }
        };
        gridContainer.add(rowsListView);

        return gridContainer;
    }

    private List<Integer> calculateVerticalPositions(List<TimelineRectangle> rectangles) {
        List<Integer> verticalPositions = new ArrayList<>();
        List<Double> endTimes = new ArrayList<>();

        for (TimelineRectangle rectangle : rectangles) {
            double startTime = calculateTimeOffset(rectangle.getStartDateTime());

            // Find the first available vertical position
            int verticalLevel = 0;
            while (verticalLevel < endTimes.size() && startTime < endTimes.get(verticalLevel)) {
                verticalLevel++;
            }

            // Extend or add to the endTimes list
            double endTime = calculateTimeOffset(rectangle.getEndDateTime());
            if (verticalLevel < endTimes.size()) {
                endTimes.set(verticalLevel, endTime);
            } else {
                endTimes.add(endTime);
            }

            verticalPositions.add(verticalLevel);
        }

        return verticalPositions;
    }

    private double calculateTimeOffset(LocalDateTime dateTime) {
        LocalDateTime timelineStart = LocalDateTime.now().minusDays(3).withHour(0).withMinute(0).withSecond(0);
        return ChronoUnit.MINUTES.between(timelineStart, dateTime) / 60.0;
    }

    // Update the calculateRectangleStyle method to use the rectangle's color
    private String calculateRectangleStyle(TimelineRectangle rectangle, int verticalLevel) {
        LocalDateTime timelineStart = LocalDateTime.now().minusDays(3).withHour(0).withMinute(0).withSecond(0);

        long minutesSinceTimelineStart = ChronoUnit.MINUTES.between(timelineStart, rectangle.getStartDateTime());
        long durationMinutes = ChronoUnit.MINUTES.between(rectangle.getStartDateTime(), rectangle.getEndDateTime());

        double leftOffset = (minutesSinceTimelineStart / 60.0) * HOUR_WIDTH;
        double width = (durationMinutes / 60.0) * HOUR_WIDTH;

        // Use the rectangle's color or fallback to COLORS array
        String color = rectangle.getColor() != null ? rectangle.getColor() : COLORS[verticalLevel % COLORS.length];

        return String.format(
                "position: absolute; " +
                        "left: %.2fpx; " +
                        "width: %.2fpx; " +
                        "background-color: %s; " +
                        "height: %dpx; " +
                        "top: %dpx; " +
                        "border-radius: 5px; " +
                        "z-index: %d; " +
                        "display: flex; " +
                        "align-items: center; " +
                        "justify-content: center; " +
                        "color: white; " +
                        "font-size: 12px; " +
                        "box-shadow: 0 2px 4px rgba(0,0,0,0.2);",
                leftOffset,
                width,
                color,
                RECTANGLE_HEIGHT,
                verticalLevel * (RECTANGLE_HEIGHT + VERTICAL_SPACING),
                verticalLevel + 10
        );
    }

    private List<TimelineRectangle> generateSampleRectangles() {
        List<TimelineRectangle> rectangles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Sample rectangles with different colors
        rectangles.add(new TimelineRectangle(
                now.plusHours(2),   // starts 2 hours from now
                now.plusHours(4),   // ends 4 hours from now
                "Meeting",
                COLORS[0] // Blue
        ));

        rectangles.add(new TimelineRectangle(
                now.plusHours(3),  // starts 3 hours from now
                now.plusHours(5),  // ends 5 hours from now
                "Discussion",
                COLORS[1] // Green
        ));

        rectangles.add(new TimelineRectangle(
                now.minusHours(1),  // starts 1 hour before now
                now.plusHours(1),   // ends 1 hour after now
                "Work Block",
                COLORS[2] // Red
        ));

        rectangles.add(new TimelineRectangle(
                now.plusDays(1).plusHours(3),  // starts 1 day and 3 hours from now
                now.plusDays(1).plusHours(5),  // ends 1 day and 5 hours from now
                "Project Review",
                COLORS[3] // Orange
        ));

        rectangles.add(new TimelineRectangle(
                now.plusHours(5),  // starts 5 hours from now
                now.plusHours(7),  // ends 7 hours from now
                "Training",
                COLORS[4] // Purple
        ));

        return rectangles;
    }
}
