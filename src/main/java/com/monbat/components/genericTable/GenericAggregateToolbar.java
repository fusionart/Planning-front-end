package com.monbat.components.genericTable;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;

import java.io.Serializable;
import java.util.Iterator;

public class GenericAggregateToolbar<T extends Serializable> extends AbstractToolbar {

    private final IDataProvider<T> dataProvider;

    public GenericAggregateToolbar(final DataTable<T, ?> table, IDataProvider<T> dataProvider) {
        super(table);
        this.dataProvider = dataProvider;

        // Create the footer row container
        WebMarkupContainer row = new WebMarkupContainer("row");
        row.setOutputMarkupId(true);
        add(row);

        // Create a RepeatingView to hold footer cells
        RepeatingView cells = new RepeatingView("cells");
        row.add(cells);

        // Loop through each column and create a footer cell
        for (IColumn<T, ?> column : table.getColumns()) {
            WebMarkupContainer cellContainer = new WebMarkupContainer(cells.newChildId());
            cells.add(cellContainer);

            // Check if column is an aggregatable column definition
            String aggregateValue = getAggregateValue(column);

            // Add the aggregated value if applicable, otherwise an empty cell
            cellContainer.add(new Label("cell", aggregateValue));
        }
    }

    private String getAggregateValue(IColumn<T, ?> column) {
        // If column is a PropertyColumnDefinition, check for aggregation
        if (column instanceof PropertyColumnDefinition) {
            PropertyColumnDefinition<?> columnDef = (PropertyColumnDefinition<?>) column;

            // Only aggregate if marked as aggregatable
            if (columnDef.isAggregatetable()) {
                return computeAggregation(columnDef.getPropertyExpression());
            }
        }
        return "";
    }

    private String computeAggregation(String propertyExpression) {
        double sum = 0;
        int count = 0;
        boolean isNumeric = false;

        // Iterate over data provider to compute sum/count
        Iterator<? extends T> iterator = dataProvider.iterator(0, dataProvider.size());
        while (iterator.hasNext()) {
            T rowObject = iterator.next();

            try {
                Object value = PropertyResolver.getValue(propertyExpression, rowObject);

                if (value instanceof Number) {
                    isNumeric = true;
                    sum += ((Number) value).doubleValue();
                }

                if (value != null) {
                    count++;
                }
            } catch (Exception e) {
                // Ignore properties that can't be resolved
            }
        }

        // Format output based on aggregation
        if (isNumeric) {
            return "Sum: " + sum + ", Count: " + count;
        } else {
            return "Count: " + count;
        }
    }
}