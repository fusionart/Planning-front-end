package com.monbat.pages.grid;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
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
import org.apache.wicket.model.PropertyModel;

import java.util.*;
import java.util.stream.Collectors;

public class Grid extends Panel {

    private final List<String> allVerticalCodes;
    private List<String> filteredVerticalCodes;
    private String filterText = "";  // Initialize as empty string instead of null
    private final WebMarkupContainer leftColumnContainer;
    private final WebMarkupContainer contentContainer;
    private final List<String> horizontalCodes;

    public Grid(String id) {
        super(id);

        // Initialize data
        allVerticalCodes = Arrays.asList("Code 1", "Code 2", "Code 3", "Code 4", "Code 5", "Code 6", "Code 7", "Code 8");
        filteredVerticalCodes = new ArrayList<>(allVerticalCodes);
        horizontalCodes = Arrays.asList("Code A", "Code B", "Code C", "Code D", "Code E", "Code F", "Code G", "Code H");

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
                // Handle null or empty filter text
                if (filterText == null || filterText.trim().isEmpty()) {
                    filteredVerticalCodes = new ArrayList<>(allVerticalCodes);
                } else {
                    filteredVerticalCodes = allVerticalCodes.stream()
                            .filter(code -> code.toLowerCase().contains(filterText.toLowerCase()))
                            .collect(Collectors.toList());
                }

                // Rebuild the left column
                leftColumnContainer.removeAll();
                ListView<String> rowItems = new ListView<>("rowItems", filteredVerticalCodes) {
                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new Label("rowText", item.getModelObject()));
                    }
                };
                leftColumnContainer.add(rowItems);

                // Rebuild the content area
                contentContainer.removeAll();
                contentContainer.add(getComponents(horizontalCodes));

                target.add(leftColumnContainer);
                target.add(contentContainer);
            }
        };
        filterForm.add(filterButton);

        // Create containers
        WebMarkupContainer topHeaderContainer = new WebMarkupContainer("topHeaderContainer");
        leftColumnContainer = new WebMarkupContainer("leftColumnContainer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisibilityAllowed(!filteredVerticalCodes.isEmpty());
            }
        };
        leftColumnContainer.setOutputMarkupId(true);

        WebMarkupContainer contentArea = new WebMarkupContainer("contentArea");
        contentContainer = new WebMarkupContainer("contentContainer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisibilityAllowed(!filteredVerticalCodes.isEmpty());
            }
        };
        contentContainer.setOutputMarkupId(true);

        excelContainer.add(topHeaderContainer);
        excelContainer.add(leftColumnContainer);
        excelContainer.add(contentArea);
        contentArea.add(contentContainer);

        // Add header items (horizontal codes)
        ListView<String> headerItems = new ListView<>("headerItems", horizontalCodes) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("headerText", item.getModelObject()));
            }
        };
        topHeaderContainer.add(headerItems);

        // Add initial row items (vertical codes)
        ListView<String> rowItems = new ListView<>("rowItems", filteredVerticalCodes) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("rowText", item.getModelObject()));
            }
        };
        leftColumnContainer.add(rowItems);

        // Create initial data for the grid cells
        contentContainer.add(getComponents(horizontalCodes));

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

    private ListView<List<Map<String, Object>>> getComponents(List<String> horizontalCodes) {
        List<List<Map<String, Object>>> cellData = new ArrayList<>();
        for (String vCode : filteredVerticalCodes) {
            List<Map<String, Object>> rowData = new ArrayList<>();
            for (String hCode : horizontalCodes) {
                rowData.add(getValueForIntersection(vCode, hCode));
            }
            cellData.add(rowData);
        }

        return new ListView<>("cellRows", cellData) {
            @Override
            protected void populateItem(ListItem<List<Map<String, Object>>> rowItem) {
                List<Map<String, Object>> row = rowItem.getModelObject();
                ListView<Map<String, Object>> cells = new ListView<>("cells", row) {
                    @Override
                    protected void populateItem(ListItem<Map<String, Object>> cellItem) {
                        Map<String, Object> cellData = cellItem.getModelObject();
                        String value = (String) cellData.get("value");
                        boolean isNegative = (Boolean) cellData.get("isNegative");

                        Label cellValue = new Label("cellValue", value);
                        cellValue.add(new AttributeModifier("class", isNegative ? "negative-value" : ""));
                        cellItem.add(cellValue);
                    }
                };
                rowItem.add(cells);
            }
        };
    }

    private Map<String, Object> getValueForIntersection(String verticalCode, String horizontalCode) {
        // Example: Generate a random negative/positive value for demo purposes
        double value = Math.random() * 200 - 100; // Range: -100 to 100
        boolean isNegative = value < 0;
        String displayText = String.format("%.2f", value); // Format to 2 decimal places

        Map<String, Object> data = new HashMap<>();
        data.put("value", displayText);
        data.put("isNegative", isNegative);
        return data;
    }
}