package com.monbat.pages.newTimeline;

import lombok.Getter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimelineHeaderPanel extends Panel {
    // Minimum and maximum hour width configuration
    private static final int HOUR_MIN_WIDTH = 10;
    private static final int HOUR_MAX_WIDTH = 50;

    // Inner class to represent a detailed timeline cell
    public static class MergedCell implements Serializable {
        @Getter
        private LocalDate startDate;
        @Getter
        private LocalDate endDate;
        private final String monthName;
        private final int weekNumber;
        private final int year;
        @Getter
        private List<DayDetails> days;
        @Getter
        private double columnWidth;

        @Getter
        public static class DayDetails implements Serializable{
            @Getter
            private final LocalDate date;
            private final List<LocalTime> hours;

            public DayDetails(LocalDate date) {
                this.date = date;
                this.hours = generateHours();
            }

            private List<LocalTime> generateHours() {
                return IntStream.range(0, 24)
                        .mapToObj(hour -> LocalTime.of(hour, 0))
                        .collect(Collectors.toList());
            }

        }

        public MergedCell(LocalDate startDate, LocalDate endDate, double columnWidth) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.monthName = startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
            this.weekNumber = startDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            this.year = startDate.getYear();
            this.columnWidth = columnWidth;

            // Collect days with their hours
            this.days = generateDaysList(startDate, endDate);
        }

        private List<DayDetails> generateDaysList(LocalDate start, LocalDate end) {
            List<DayDetails> daysList = new ArrayList<>();
            LocalDate current = start;
            while (!current.isAfter(end)) {
                daysList.add(new DayDetails(current));
                current = current.plusDays(1);
            }
            return daysList;
        }

        public String getDisplayText() {
            // If spanning multiple months, show both month names
            if (startDate.getMonth() != endDate.getMonth()) {
                return String.format("%s-%s %d | Week %d",
                        startDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        endDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        year, weekNumber);
            }

            // Single month scenario
            return String.format("%s %d | Week %d",
                    monthName, year, weekNumber);
        }
    }

    public TimelineHeaderPanel(String id) {
        this(id, 200.0, 20.0); // Default column width and hour width
    }

    public TimelineHeaderPanel(String id, double columnWidth) {
        this(id, columnWidth, 20.0); // Default hour width
    }

    public TimelineHeaderPanel(String id, double columnWidth, double hourWidth) {
        super(id);

        // Validate and adjust hour width
        double adjustedHourWidth = Math.max(HOUR_MIN_WIDTH,
                Math.min(hourWidth, HOUR_MAX_WIDTH));

        // Calculate date range
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3);
        LocalDate endDate = today.plusDays(10);

        // Generate merged cells
        List<MergedCell> mergedCells = generateMergedCells(startDate, endDate, columnWidth);

        // Create ListView for merged cells
        ListView<MergedCell> cellListView = new ListView<>("mergedCellList", mergedCells) {
            @Override
            protected void populateItem(ListItem<MergedCell> item) {
                MergedCell cell = item.getModelObject();

                // Set dynamic width
                item.add(new AttributeModifier("style",
                        Model.of(String.format("width: %.2fpx;", cell.getColumnWidth()))));

                // Main cell information
                Label headerLabel = new Label("headerLabel", cell.getDisplayText());
                item.add(headerLabel);

                // Create ListView for days within this cell
                ListView<MergedCell.DayDetails> daysListView =
                        new ListView<>("daysList", cell.getDays()) {
                            @Override
                            protected void populateItem(ListItem<MergedCell.DayDetails> dayItem) {
                                MergedCell.DayDetails dayDetails = dayItem.getModelObject();
                                LocalDate day = dayDetails.getDate();

                                // Day of month label
                                Label dayLabel = new Label("dayLabel",
                                        day.format(DateTimeFormatter.ofPattern("dd"))
                                );
                                dayItem.add(dayLabel);

                                // Day of week label
                                Label weekdayLabel = new Label("weekdayLabel",
                                        day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                );
                                dayItem.add(weekdayLabel);

                                // Hours ListView
                                ListView<LocalTime> hoursListView =
                                        new ListView<>("hoursList", dayDetails.getHours()) {
                                            @Override
                                            protected void populateItem(ListItem<LocalTime> hourItem) {
                                                LocalTime hour = hourItem.getModelObject();

                                                Label hourLabel = new Label("hourLabel",
                                                        hour.format(DateTimeFormatter.ofPattern("HH"))
                                                );

                                                // Add style for hour width
                                                hourItem.add(new AttributeModifier("style",
                                                        Model.of(String.format("min-width: %.2fpx; max-width: %.2fpx;",
                                                                adjustedHourWidth, adjustedHourWidth))));

                                                hourItem.add(hourLabel);
                                            }
                                        };
                                dayItem.add(hoursListView);

                                // Highlight today
                                if (day.equals(LocalDate.now())) {
                                    dayItem.add(new AttributeModifier("class", Model.of("today-day")));
                                }
                            }
                        };
                item.add(daysListView);

                // Add tooltip with full date range
                String tooltipText = String.format("%s - %s",
                        cell.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        cell.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                item.add(new AttributeModifier("title", Model.of(tooltipText)));

                // Highlight if the cell contains today
                LocalDate today = LocalDate.now();
                if (today.isAfter(cell.getStartDate().minusDays(1)) &&
                        today.isBefore(cell.getEndDate().plusDays(1))) {
                    item.add(new AttributeModifier("class", Model.of("merged-cell today-cell")));
                } else {
                    item.add(new AttributeModifier("class", Model.of("merged-cell")));
                }
            }
        };

        add(cellListView);
    }

    private List<MergedCell> generateMergedCells(LocalDate startDate, LocalDate endDate, double columnWidth) {
        List<MergedCell> mergedCells = new ArrayList<>();
        Map<Integer, List<LocalDate>> weekGroups = groupDatesByWeek(startDate, endDate);

        for (Map.Entry<Integer, List<LocalDate>> entry : weekGroups.entrySet()) {
            List<LocalDate> weekDates = entry.getValue();
            mergedCells.add(new MergedCell(
                    weekDates.get(0),
                    weekDates.get(weekDates.size() - 1),
                    columnWidth
            ));
        }

        return mergedCells;
    }

    private Map<Integer, List<LocalDate>> groupDatesByWeek(LocalDate startDate, LocalDate endDate) {
        // Group dates by their week number
        Map<Integer, List<LocalDate>> weekGroups = new LinkedHashMap<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            int weekNumber = currentDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

            weekGroups.computeIfAbsent(weekNumber, k -> new ArrayList<>()).add(currentDate);

            currentDate = currentDate.plusDays(1);
        }

        return weekGroups;
    }
}
