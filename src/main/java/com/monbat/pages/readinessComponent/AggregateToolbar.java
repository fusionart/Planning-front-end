package com.monbat.pages.readinessComponent;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;

import java.util.Iterator;

public class AggregateToolbar<T, S> extends AbstractToolbar {

    private final IDataProvider<T> dataProvider;

    public AggregateToolbar(final DataTable<T, S> table, IDataProvider<T> dataProvider) {
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
        for (IColumn<T, S> column : table.getColumns()) {
            WebMarkupContainer cellContainer = new WebMarkupContainer(cells.newChildId());
            cells.add(cellContainer);

            // Try to determine if the column should be summed or counted
            String aggregateValue = getAggregateValue(column);

            // Add the aggregated value if applicable, otherwise an empty cell
            cellContainer.add(new Label("cell", aggregateValue));
        }
    }

    private String getAggregateValue(IColumn<T, S> column) {
        double sum = 0;
        int count = 0;
        boolean summable = false;
        boolean countable = false;

        // Determine column properties dynamically
        if (column instanceof PropertyColumn) {
            String propertyExpression = ((PropertyColumn<T, S>) column).getPropertyExpression();
            summable = shouldBeSummed(propertyExpression);
            countable = shouldBeCounted(propertyExpression);
        }

        // Iterate over data provider to compute sum/count
        Iterator<? extends T> iterator = dataProvider.iterator(0, dataProvider.size());
        while (iterator.hasNext()) {
            T rowObject = iterator.next();
            Object value = getColumnValue(column, rowObject);

            if (value instanceof Number) {
                sum += ((Number) value).doubleValue();
            }

            if (value != null) {
                count++;
            }
        }

        // Format output based on aggregation rules
        if (summable && countable) {
            return "Sum: " + sum + ", Count: " + count;
        } else if (summable) {
            return "Sum: " + sum;
        } else if (countable) {
            return "Count: " + count;
        }
        return "";
    }

    private Object getColumnValue(IColumn<T, S> column, T rowObject) {
        if (column instanceof PropertyColumn) {
            String propertyExpression = ((PropertyColumn<T, S>) column).getPropertyExpression();
            try {
                return PropertyResolver.getValue(propertyExpression, rowObject);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private boolean shouldBeSummed(String property) {
        return property.matches(".*(price|amount|quantity|total).*"); // Adjust based on real column names
    }

    private boolean shouldBeCounted(String property) {
        return property.matches(".*(id|serial|number|count).*"); // Adjust as needed
    }
}


