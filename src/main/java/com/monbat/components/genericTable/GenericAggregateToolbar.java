package com.monbat.components.genericTable;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.function.Supplier;

public class GenericAggregateToolbar<T extends Serializable> extends AbstractToolbar {

    private final Supplier<Iterator<? extends T>> dataSupplier;

    public GenericAggregateToolbar(final DataTable<T, ?> table, Supplier<Iterator<? extends T>> dataSupplier) {
        super(table);
        this.dataSupplier = dataSupplier;
        initializeToolbar();
    }

    // Overloaded constructor that accepts a data provider and converts it to a supplier
    public GenericAggregateToolbar(final DataTable<T, ?> table,
                                   org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider<T, String> dataProvider) {
        super(table);
        this.dataSupplier = () -> dataProvider.iterator(0, dataProvider.size());
        initializeToolbar();
    }

    // Debug method to print data structure
    private void debugDataStructure() {
        System.out.println("=== DEBUG: Data Structure ===");
        try {
            Iterator<? extends T> iterator = dataSupplier.get();
            int itemCount = 0;
            while (iterator.hasNext() && itemCount < 3) { // Only check first 3 items
                T rowObject = iterator.next();
                itemCount++;

                if (rowObject instanceof com.monbat.models.dto.sap.sales_order.SalesOrderMain) {
                    @SuppressWarnings("unchecked")
                    com.monbat.models.dto.sap.sales_order.SalesOrderMain salesOrderMain =
                            (com.monbat.models.dto.sap.sales_order.SalesOrderMain) rowObject;

                    System.out.println("Item " + itemCount + ":");
                    System.out.println("  Material: " + salesOrderMain.getMaterial());
                    System.out.println("  Requested Quantity: " + salesOrderMain.getRequestedQuantity());

                    if (salesOrderMain.getDynamicSoItems() != null) {
                        System.out.println("  Dynamic SO Items:");
                        for (java.util.Map.Entry<String, com.monbat.models.dto.sap.sales_order.SalesOrderMainItem> entry :
                                salesOrderMain.getDynamicSoItems().entrySet()) {
                            System.out.println("    SO " + entry.getKey() + ": quantity=" +
                                    (entry.getValue() != null ? entry.getValue().getQuantity() : "null"));
                        }
                    } else {
                        System.out.println("  Dynamic SO Items: null");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in debugDataStructure: " + e.getMessage());
        }
        System.out.println("=== END DEBUG ===");
    }

    private void initializeToolbar() {
        // Debug the data structure first
        debugDataStructure();

        // Create the footer row container
        WebMarkupContainer row = new WebMarkupContainer("row");
        row.setOutputMarkupId(true);
        add(row);

        // Create a RepeatingView to hold footer cells
        RepeatingView cells = new RepeatingView("cells");
        row.add(cells);

        // Loop through each column and create a footer cell
        for (IColumn<?, ?> column : getTable().getColumns()) {
            WebMarkupContainer cellContainer = new WebMarkupContainer(cells.newChildId());
            cells.add(cellContainer);

            // Check if column is an aggregatable column definition
            String aggregateValue = getAggregateValue((IColumn<T, ?>) column);

            // Add the aggregated value if applicable, otherwise an empty cell
            Label cellLabel = new Label("cell", aggregateValue);
            cellLabel.setEscapeModelStrings(false); // Allow HTML content if needed
            cellContainer.add(cellLabel);
        }
    }

    private String getAggregateValue(IColumn<T, ?> column) {
        try {
            System.out.println("=== Processing column ===");
            System.out.println("Class: " + column.getClass().getSimpleName());
            System.out.println("Full class name: " + column.getClass().getName());

            // Check if this is our custom AggregatablePropertyColumn
            if (column instanceof AggregatablePropertyColumn) {
                AggregatablePropertyColumn<?, ?> propertyCol = (AggregatablePropertyColumn<?, ?>) column;

                System.out.println("AggregatablePropertyColumn found: " + propertyCol.getHeader() +
                        ", Aggregatable: " + propertyCol.isAggregatable() +
                        ", Property: " + propertyCol.getPropertyExpression());

                // Always compute something - either sum for aggregatable or count for non-aggregatable
                if (propertyCol.isAggregatable()) {
                    String result = computeAggregation(propertyCol.getPropertyExpression());
                    System.out.println("Computed aggregation for " + propertyCol.getHeader() + ": " + result);
                    return result;
                } else {
                    String result = computeCount();
                    System.out.println("Computed count for " + propertyCol.getHeader() + ": " + result);
                    return result;
                }
            }

            // Check if this is a PropertyColumnDefinition that supports aggregation (fallback)
            if (column instanceof PropertyColumnDefinition) {
                @SuppressWarnings("unchecked")
                PropertyColumnDefinition<T> columnDef = (PropertyColumnDefinition<T>) column;

                System.out.println("PropertyColumnDefinition found: " + columnDef.getHeader() + ", Aggregatable: " + columnDef.isAggregatetable());

                // Always compute something - either sum for aggregatable or count for non-aggregatable
                if (columnDef.isAggregatetable()) {
                    String result = computeAggregation(columnDef.getPropertyExpression());
                    System.out.println("Computed aggregation for " + columnDef.getHeader() + ": " + result);
                    return result;
                } else {
                    String result = computeCount();
                    System.out.println("Computed count for " + columnDef.getHeader() + ": " + result);
                    return result;
                }
            }

            // Check if this is our custom AggregatableDynamicColumn
            if (column instanceof AggregatableDynamicColumn) {
                AggregatableDynamicColumn dynamicCol = (AggregatableDynamicColumn) column;

                System.out.println("AggregatableDynamicColumn found: " + dynamicCol.getHeader() +
                        ", SO: " + dynamicCol.getSalesOrderNumber() +
                        ", Type: " + dynamicCol.getSubColumnType() +
                        ", Aggregatable: " + dynamicCol.isAggregatable());

                // Always compute something - either sum for aggregatable or count for non-aggregatable
                if (dynamicCol.isAggregatable()) {
                    String result = computeDynamicAggregation(dynamicCol);
                    System.out.println("Computed dynamic aggregation: " + result);
                    return result;
                } else {
                    String result = computeCount();
                    System.out.println("Computed count for dynamic column: " + result);
                    return result;
                }
            }

            // For any other column type, show count
            String result = computeCount();
            System.out.println("Computed count for unknown column type: " + result);
            return result;

        } catch (Exception e) {
            System.err.println("Error processing column: " + e.getMessage());
            e.printStackTrace();
        }

        return ""; // Fallback
    }

    private String computeAggregation(String propertyExpression) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        boolean isNumeric = false;
        int totalItems = 0;

        try {
            Iterator<? extends T> iterator = dataSupplier.get();

            while (iterator.hasNext()) {
                T rowObject = iterator.next();
                totalItems++;

                try {
                    Object value = PropertyResolver.getValue(propertyExpression, rowObject);

                    if (value instanceof Number) {
                        isNumeric = true;
                        BigDecimal numValue = new BigDecimal(value.toString());
                        sum = sum.add(numValue);
                        count++;
                    }

                } catch (Exception e) {
                    // Ignore properties that can't be resolved
                }
            }

            System.out.println("Aggregation summary for " + propertyExpression +
                    ": totalItems=" + totalItems + ", numericCount=" + count + ", sum=" + sum + ", isNumeric=" + isNumeric);

        } catch (Exception e) {
            System.err.println("Error in computeAggregation: " + e.getMessage());
            e.printStackTrace();
        }

        // Format output based on aggregation
        if (isNumeric && count > 0) {
            return "Σ: " + sum.setScale(2, RoundingMode.HALF_UP);
        } else if (totalItems > 0) {
            return "Count: " + totalItems;
        }

        return "";
    }

    private String computeCount() {
        int count = 0;

        try {
            Iterator<? extends T> iterator = dataSupplier.get();
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }

            System.out.println("Total count: " + count);

        } catch (Exception e) {
            System.err.println("Error in computeCount: " + e.getMessage());
            e.printStackTrace();
        }

        return count > 0 ? "Count: " + count : "";
    }

    private String computeDynamicAggregation(AggregatableDynamicColumn dynamicCol) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        int totalItems = 0;

        try {
            Iterator<? extends T> iterator = dataSupplier.get();
            while (iterator.hasNext()) {
                T rowObject = iterator.next();
                totalItems++;

                try {
                    // For SalesOrderMain objects, use the column's method
                    if (rowObject instanceof com.monbat.models.dto.sap.sales_order.SalesOrderMain) {
                        @SuppressWarnings("unchecked")
                        com.monbat.models.dto.sap.sales_order.SalesOrderMain salesOrderMain =
                                (com.monbat.models.dto.sap.sales_order.SalesOrderMain) rowObject;

                        Double value = dynamicCol.getNumericValue(salesOrderMain);
                        if (value != null) {
                            BigDecimal quantity = new BigDecimal(value.toString());
                            sum = sum.add(quantity);
                            count++;
                            System.out.println("Added value for SO " + dynamicCol.getSalesOrderNumber() +
                                    " in material " + salesOrderMain.getMaterial() + ": " + value);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing dynamic column: " + e.getMessage());
                }
            }

            System.out.println("Dynamic aggregation for " + dynamicCol.getSalesOrderNumber() +
                    ": totalItems=" + totalItems + ", validCount=" + count + ", sum=" + sum);

        } catch (Exception e) {
            System.err.println("Error in computeDynamicAggregation: " + e.getMessage());
            e.printStackTrace();
        }

        if (count > 0) {
            return "Σ: " + sum.setScale(2, RoundingMode.HALF_UP);
        }

        return "";
    }
}