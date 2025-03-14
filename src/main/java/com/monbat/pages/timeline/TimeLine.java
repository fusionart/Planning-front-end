package com.monbat.pages.timeline;

import com.monbat.models.dto.ReadinessByWeek;
import com.monbat.models.dto.ReadinessDetailWithDate;
import com.monbat.models.entities.CellData;
import com.monbat.models.entities.TableData;
import com.monbat.services.LoadReadinessByWeek;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.WeekFields;
import java.util.*;

public class TimeLine extends Panel {
    private List<TableData> tableDataList;
    private List<TableData> newTableDataList = new ArrayList<>();
    private final List<String> boundaryColors = Arrays.asList("orange", "blue", "green", "red");

    private List<String> months = new ArrayList<>();
    private Map<String, String> monthBoundaryColors = new HashMap<>();
    private Map<String, List<String>> monthWeeks = new LinkedHashMap<>();
    private int year;

    public TimeLine(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Create a container for the table with scrollbars
        WebMarkupContainer scrollableContainer = new WebMarkupContainer("scrollableContainer");
        scrollableContainer.add(new AttributeModifier("class", "scrollable-container"));
        add(scrollableContainer);

        // Create the table
        WebMarkupContainer table = new WebMarkupContainer("table");
        table.add(new AttributeModifier("class", "timeline-table"));
        scrollableContainer.add(table);

        //TODO add logic to show that data is loading
//        add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
//            @Override
//            protected void onTimer(AjaxRequestTarget target) {
//                List<ReadinessByWeek> readinessData = LoadReadinessByWeek.getReadinessByWeek();
//                System.out.println("Readiness: " + readinessData.size());
//
//                if (readinessData != null) {
//                    newTableDataList = createTableDataList(readinessData);
//                }
//            }
//        });

        newTableDataList = createTableDataList(LoadReadinessByWeek.getReadinessByWeek());

        // Create a flat list of all weeks
        List<String> allWeeks = new ArrayList<>();
        for (List<String> weeks : monthWeeks.values()) {
            allWeeks.addAll(weeks);
        }

        // Create month header row
        WebMarkupContainer monthRow = new WebMarkupContainer("monthRow");
        table.add(monthRow);

        // Add an empty cell for the top-left corner
        Label cornerCell = new Label("cornerCell", "");
        monthRow.add(cornerCell);

        // Create month headers
        ListView<String> monthsList = new ListView<>("months", months) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String month = item.getModelObject();
                List<String> weeks = monthWeeks.get(month);

                // Create the month label
                Label monthLabel = new Label("monthLabel", month + " " + year);
                monthLabel.add(new AttributeModifier("colspan", weeks.size()));
                monthLabel.add(new AttributeModifier("class", "month-header"));

                // Add the border color for the month
                String borderColor = monthBoundaryColors.get(month);
                if (borderColor != null) {
                    monthLabel.add(new AttributeModifier("style", "border-right: 2px solid " + borderColor + ";"));
                }

                item.add(monthLabel);
            }
        };
        monthRow.add(monthsList);

        // Create week header row
        WebMarkupContainer weekRow = new WebMarkupContainer("weekRow");
        table.add(weekRow);

        // Add the row header label
        Label weekRowLabel = new Label("weekRowLabel", "");
        weekRow.add(weekRowLabel);

        // Create week headers
        ListView<String> weeks = new ListView<>("weeks", allWeeks) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String week = item.getModelObject();

                // Create the week label
                Label weekLabel = new Label("weekLabel", "W. " + week);
                weekLabel.add(new AttributeModifier("class", "week-header"));

//                // Add special styling for the right border of the last week in a month
                if (week.equals("9") || week.equals("14") || week.equals("18")) {
                    String borderColor = week.equals("9") ? "orange" : (week.equals("14") ? "blue" : "green");
                    weekLabel.add(new AttributeModifier("style", "border-right: 2px solid " + borderColor + ";"));
                }

                item.add(weekLabel);
            }
        };
        weekRow.add(weeks);

        ListView<TableData> dataRows = new ListView<>("dataRows", newTableDataList) {
            @Override
            protected void populateItem(ListItem<TableData> item) {
                TableData rowData = item.getModelObject();

                // Add row label
                Label rowLabel = new Label("rowLabel", rowData.getRowLabel());
                rowLabel.add(new AttributeModifier("class", "row-label"));
                item.add(rowLabel);

                // Add cells for each week
                ListView<String> cells = new ListView<>("cells", allWeeks) {
                    @Override
                    protected void populateItem(ListItem<String> cellItem) {
                        String week = cellItem.getModelObject();

                        // Get the cell data
                        CellData cellData = rowData.getCellData()
                                .getOrDefault("ALL_WEEKS", new HashMap<>())
                                .getOrDefault(Integer.parseInt(week), new CellData("0", "white"));

                        // Create the cell
                        WebMarkupContainer cell = new WebMarkupContainer("cell");

                        // Add the cell content
                        Label cellText = new Label("cellText", cellData.getText());
                        cell.add(cellText);

                        // Add special styling for the right border of the last week in a month
//                        if (week.equals("9") || week.equals("14") || week.equals("18")) {
//                            String borderColor = week.equals("9") ? "orange" : (week.equals("14") ? "blue" : "green");
//                            cell.add(new AttributeModifier("style", "border-right: 2px solid " + borderColor + ";"));
//                        }

                        cellItem.add(cell);
                    }
                };
                item.add(cells);
            }
        };
        table.add(dataRows);
    }

    private List<TableData> createTableDataList(List<ReadinessByWeek> readinessData) {
        List<TableData> tableDataList = new ArrayList<>();

        for (ReadinessByWeek item : readinessData) {
            for (Map.Entry<String, List<ReadinessDetailWithDate>> readinessByDateEntry : item.getMap().entrySet()) {
                for (ReadinessDetailWithDate detail : readinessByDateEntry.getValue()) {
                    int weekNum = Integer.parseInt(StringUtils.left(readinessByDateEntry.getKey(), 2));
                    year = Integer.parseInt(StringUtils.right(readinessByDateEntry.getKey(), 4));

                    LocalDate date = LocalDate.of(year, 1, 1)
                            .with(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(), weekNum);

                    if (!months.contains(date.getMonth().toString())) {
                        months.add(date.getMonth().toString());
                        monthBoundaryColors.put(date.getMonth().toString(), boundaryColors.get(months.size() - 1));
                    }

                    if (!monthWeeks.containsKey(date.getMonth().toString())) {
                        monthWeeks.put(date.getMonth().toString(), new ArrayList<>());
                        monthWeeks.get(date.getMonth().toString()).add(String.valueOf(weekNum));
                    } else {
                        if (!monthWeeks.get(date.getMonth().toString()).contains(String.valueOf(weekNum))) {
                            monthWeeks.get(date.getMonth().toString()).add(String.valueOf(weekNum));
                        }
                    }

                    String material = detail.getDetail().getMaterial();
                    TableData tableData = tableDataList.stream()
                            .filter(td -> td.getRowLabel().equals(material))
                            .findFirst()
                            .orElse(null);
                    if (tableData == null) {
                        Map<String, Map<Integer, CellData>> cellDataMap = new HashMap<>();
                        Map<Integer, CellData> weekData = new HashMap<>();
                        cellDataMap.put("ALL_WEEKS", weekData);
                        tableData = new TableData(material, cellDataMap);
                        tableDataList.add(tableData);
                    }

                    int weekNumber = Integer.parseInt(StringUtils.left(readinessByDateEntry.getKey(), 2));
                    tableData.getCellData().get("ALL_WEEKS").put(weekNumber,
                            new CellData(String.valueOf(detail.getDetail().getOrderQuantity()), "white"));
                }
            }
        }
        return tableDataList;
    }
}
