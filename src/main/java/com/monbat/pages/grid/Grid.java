package com.monbat.pages.grid;

import com.monbat.models.dto.GridDetailsDto;
import com.monbat.models.dto.GridDto;
import com.monbat.models.dto.ReadinessByWeek;
import com.monbat.models.dto.ReadinessDetailWithDate;
import com.monbat.models.entities.BatteryQuantity;
import com.monbat.services.LoadBatteryQuantity;
import com.monbat.services.LoadReadinessByWeek;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.*;
import java.util.stream.Collectors;

public class Grid extends Panel {

    private List<String> allVerticalCodes;
    private List<String> filteredVerticalCodes;
    private String filterText = "";
    private final WebMarkupContainer leftColumnContainer;
    private final WebMarkupContainer contentContainer;
    private final WebMarkupContainer topHeaderContainer;
    private final List<String> horizontalCodes;
    private GridDto gridDto = null;
    private List<BatteryQuantity> batteryQuantityListLocation2000 = new ArrayList<>();
    private List<BatteryQuantity> batteryQuantityListByPrefix = new ArrayList<>();
    private final Map<String, Boolean> showZeroValuesMap = new HashMap<>();

    public Grid(String id) {
        super(id);

        // Initialize data
        List<ReadinessByWeek> readinessDataList = LoadReadinessByWeek.getReadinessByWeek();
        this.batteryQuantityListLocation2000 = LoadBatteryQuantity.getBatteryQuantity(2000);
        this.batteryQuantityListByPrefix = LoadBatteryQuantity.getBatteryQuantityByPrefix(20);
        this.gridDto = convertReadinessByWeekToGridDto(readinessDataList);

        // Initialize collections
        Set<String> allWeeks = new TreeSet<>();
        Set<String> allMaterials = new TreeSet<>();

        // Populate weeks and materials
        for (String week : gridDto.getMap().keySet()) {
            allWeeks.add(week);
            showZeroValuesMap.put(week, true);
            gridDto.get(week).stream()
                    .map(GridDetailsDto::getMaterial)
                    .forEach(allMaterials::add);
        }

        this.horizontalCodes = new ArrayList<>(allWeeks);
        this.allVerticalCodes = new ArrayList<>(allMaterials);
        this.filteredVerticalCodes = new ArrayList<>(allVerticalCodes); // Initialize filteredVerticalCodes

        // Create the main container
        WebMarkupContainer excelContainer = new WebMarkupContainer("excelContainer");
        add(excelContainer);

        // Create filter form
        Form<Void> filterForm = new Form<>("filterForm");
        excelContainer.add(filterForm);

        TextField<String> filterField = new TextField<>("filterInput", new PropertyModel<>(this, "filterText"));
        filterForm.add(filterField);

        AjaxButton filterButton = new AjaxButton("filterButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (filterText == null || filterText.trim().isEmpty()) {
                    filteredVerticalCodes = new ArrayList<>(allVerticalCodes);
                } else {
                    filteredVerticalCodes = allVerticalCodes.stream()
                            .filter(code -> code.toLowerCase().contains(filterText.toLowerCase()))
                            .collect(Collectors.toList());
                }

                leftColumnContainer.removeAll();
                ListView<String> rowItems = new ListView<>("rowItems", filteredVerticalCodes) {
                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new Label("rowText", item.getModelObject()));
                    }
                };
                leftColumnContainer.add(rowItems);

                contentContainer.removeAll();
                contentContainer.add(getComponents(horizontalCodes));

                target.add(leftColumnContainer);
                target.add(contentContainer);
            }
        };
        filterForm.add(filterButton);

        // Create containers
        topHeaderContainer = new WebMarkupContainer("topHeaderContainer");
        topHeaderContainer.setOutputMarkupId(true);
        excelContainer.add(topHeaderContainer);

        leftColumnContainer = new WebMarkupContainer("leftColumnContainer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisibilityAllowed(!filteredVerticalCodes.isEmpty());
            }
        };
        leftColumnContainer.setOutputMarkupId(true);
        excelContainer.add(leftColumnContainer);

        WebMarkupContainer contentArea = new WebMarkupContainer("contentArea");
        contentContainer = new WebMarkupContainer("contentContainer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisibilityAllowed(!filteredVerticalCodes.isEmpty());
            }
        };
        contentContainer.setOutputMarkupId(true);
        excelContainer.add(contentArea);
        contentArea.add(contentContainer);

        // Create header items with checkboxes
        ListView<String> headerItems = createHeaderItemsWithCheckboxes();
        topHeaderContainer.add(headerItems);

        // Add initial row items
        ListView<String> rowItems = new ListView<>("rowItems", filteredVerticalCodes) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("rowText", item.getModelObject()));
            }
        };
        leftColumnContainer.add(rowItems);

        // Create initial grid data
        contentContainer.add(getComponents(horizontalCodes));

        // Add scroll behavior
        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                response.render(OnDomReadyHeaderItem.forScript(
                        "const contentArea = document.querySelector('.content-area');" +
                                "const topHeader = document.querySelector('.top-header');" +
                                "const leftColumn = document.querySelector('.left-column');" +
                                "contentArea.addEventListener('scroll', function() {" +
                                "    topHeader.scrollLeft = contentArea.scrollLeft;" +
                                "    leftColumn.scrollTop = contentArea.scrollTop;" +
                                "});"
                ));
            }
        });
    }

    private ListView<String> createHeaderItemsWithCheckboxes() {
        return new ListView<>("headerItems", horizontalCodes) {
            @Override
            protected void populateItem(ListItem<String> item) {
                String week = item.getModelObject();

                // Week header
                item.add(new Label("weekHeader", week));

                // Add the checkbox above the week header
                AjaxCheckBox showZeroValues = new AjaxCheckBox("showZeroValues",
                        Model.of(showZeroValuesMap.getOrDefault(week, true))) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        showZeroValuesMap.put(week, getModelObject());
                        List<String> visibleCodes = getVisibleMaterialCodes();
                        filteredVerticalCodes = visibleCodes;

                        leftColumnContainer.removeAll();
                        ListView<String> rowItems = new ListView<>("rowItems", filteredVerticalCodes) {
                            @Override
                            protected void populateItem(ListItem<String> item) {
                                item.add(new Label("rowText", item.getModelObject()));
                            }
                        };
                        leftColumnContainer.add(rowItems);

                        contentContainer.removeAll();
                        contentContainer.add(getComponents(horizontalCodes));

                        target.add(leftColumnContainer);
                        target.add(contentContainer);
                    }
                };
                item.add(showZeroValues);
            }
        };
    }

    // Helper method to determine which material codes should be visible
    private List<String> getVisibleMaterialCodes() {
        // If filter text is applied, start with filtered list; otherwise, start with all
        List<String> baseCodes = filterText == null || filterText.trim().isEmpty() ?
                new ArrayList<>(allVerticalCodes) :
                allVerticalCodes.stream()
                        .filter(code -> code.toLowerCase().contains(filterText.toLowerCase()))
                        .collect(Collectors.toList());

        // If all checkboxes are checked, return all filtered codes
        if (showZeroValuesMap.values().stream().allMatch(v -> v)) {
            return baseCodes;
        }

        // Otherwise filter out codes with empty values in unchecked columns
        List<String> visibleCodes = new ArrayList<>();

        for (String material : baseCodes) {
            boolean includeCode = true;

            for (String week : horizontalCodes) {
                boolean showZeros = showZeroValuesMap.getOrDefault(week, true);

                // If the checkbox is unchecked, check if the value is empty
                if (!showZeros) {
                    Map<String, Object> cellData = getValueForIntersection(material, week);
                    String value = (String) cellData.get("value");
                    boolean isEmptyOrZero = value.equals("0") || value.equals("-");

                    if (isEmptyOrZero) {
                        includeCode = false;
                        break;
                    }
                }
            }

            if (includeCode) {
                visibleCodes.add(material);
            }
        }

        return visibleCodes;
    }

    private GridDto convertReadinessByWeekToGridDto(List<ReadinessByWeek> readinessDataList) {
        GridDto gridDto = new GridDto();

        // Initialize the ArrayLists for qty11 and qty20
        List<GridDetailsDto> qty11List = new ArrayList<>();
        List<GridDetailsDto> qty20List = new ArrayList<>();

        for (ReadinessByWeek readinessData : readinessDataList) {
            for (Map.Entry<String, List<ReadinessDetailWithDate>> entry : readinessData.getMap().entrySet()) {
                String week = entry.getKey();

                // Group by material and sum quantities
                Map<String, GridDetailsDto> materialMap = new HashMap<>();

                for (ReadinessDetailWithDate item : entry.getValue()) {
                    String material = item.getDetail().getMaterial();
                    Integer quantity = item.getDetail().getOrderQuantity() != null ? item.getDetail().getOrderQuantity() : 0;

                    // Get the available quantities for 11 and 20
                    Integer availableQty11 = getAvailableQuantityFor11(material);
                    Integer availableQty20 = getAvailableQuantityFor20(material);

                    // Add to the respective lists
                    qty11List.add(new GridDetailsDto(material, availableQty11));
                    qty20List.add(new GridDetailsDto(material, availableQty20));

                    if (materialMap.containsKey(material)) {
                        // If material exists, sum the quantities
                        GridDetailsDto existingMaterial = materialMap.get(material);
                        existingMaterial.setQuantity(existingMaterial.getQuantity() + quantity);
                    } else {
                        // If material doesn't exist, create new entry
                        materialMap.put(material, new GridDetailsDto(
                                material,
                                quantity
                        ));
                    }
                }
                // Convert the map values to list and put in gridDto
                gridDto.put(week, new ArrayList<>(materialMap.values()));
            }
        }
        // Add the custom keys to gridDto
        gridDto.put("qty11", qty11List);
        gridDto.put("qty20", qty20List);

        return gridDto;
    }

    private ListView<List<Map<String, Object>>> getComponents(List<String> horizontalCodes) {
        List<List<Map<String, Object>>> cellData = new ArrayList<>();
        for (String material : filteredVerticalCodes) {
            List<Map<String, Object>> rowData = new ArrayList<>();
            for (String week : horizontalCodes) {
                rowData.add(getValueForIntersection(material, week));
            }
            cellData.add(rowData);
        }

        return new ListView<>("cellRows", cellData) {
            @Override
            protected void populateItem(ListItem<List<Map<String, Object>>> rowItem) {
                List<Map<String, Object>> row = rowItem.getModelObject();

                // Main data cells
                ListView<Map<String, Object>> cells = new ListView<>("cells", row) {
                    @Override
                    protected void populateItem(ListItem<Map<String, Object>> cellItem) {
                        Map<String, Object> cellData = cellItem.getModelObject();
                        WebMarkupContainer cellContainer = new WebMarkupContainer("cellContainer");
                        boolean isNegative = (Boolean) cellData.get("isNegative");

                        Label cellValue = new Label("cellValue", (String) cellData.get("value"));
                        cellValue.add(new AttributeModifier("class", isNegative ? "negative-value" : ""));
                        cellContainer.add(cellValue);

                        cellItem.add(cellContainer);
                    }
                };
                rowItem.add(cells);
            }
        };
    }

    private Map<String, Object> getValueForIntersection(String material, String week) {
        List<GridDetailsDto> weekData = gridDto.get(week);

        if (weekData == null) {
            return createEmptyCell();
        }

        Optional<GridDetailsDto> matchingItem = weekData.stream()
                .filter(item -> material.equals(item.getMaterial()))
                .findFirst();

        if (matchingItem.isPresent()) {
            GridDetailsDto item = matchingItem.get();
            boolean isNegative = item.getQuantity() != null && item.getQuantity() < 0;

            return Map.of(
                    "value", item.getQuantity() != null ? String.valueOf(item.getQuantity()) : "-",
                    "isNegative", isNegative
            );
        }

        return createEmptyCell();
    }

    private Map<String, Object> createEmptyCell() {
        return Map.of(
                "value", "-",
                "isNegative", false
        );
    }

    private Integer getAvailableQuantityFor20(String material) {
        String newMaterial = "20" + StringUtils.right(material, material.length() - 2);
        String finalNewMaterial = StringUtils.left(newMaterial, newMaterial.length() - 1) + "2";
        return batteryQuantityListByPrefix.stream()
                .filter(entity -> finalNewMaterial.equals(entity.getBatteryCode()))
                .mapToInt(BatteryQuantity::getQuantity)
                .sum();
    }

    private Integer getAvailableQuantityFor11(String material) {
        String newMaterial = "11" + StringUtils.right(material, material.length() - 2);
        return batteryQuantityListLocation2000.stream()
                .filter(entity -> newMaterial.equals(entity.getBatteryCode()))
                .mapToInt(BatteryQuantity::getQuantity)
                .sum();
    }
}