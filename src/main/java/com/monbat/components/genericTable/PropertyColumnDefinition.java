package com.monbat.components.genericTable;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.Model;

import java.io.Serializable;

public class PropertyColumnDefinition<T extends Serializable> implements ColumnDefinition<T>, Serializable {
    private final String header;
    private final String propertyExpression;
    @Getter
    @Setter
    private boolean sortable;
    @Getter
    @Setter
    private boolean aggregatetable;

    public PropertyColumnDefinition(String header, String propertyExpression) {
        this(header, propertyExpression, true, false);
    }

    public PropertyColumnDefinition(String header, String propertyExpression, boolean sortable, boolean aggregatetable) {
        this.header = header;
        this.propertyExpression = propertyExpression;
        this.sortable = sortable;
        this.aggregatetable = aggregatetable;
    }

    @Override
    public IColumn<T, String> createColumn() {
        // Use our custom AggregatablePropertyColumn instead of regular PropertyColumn
        return new AggregatablePropertyColumn<>(
                Model.of(header),
                sortable ? propertyExpression : null,  // Sort property (null if not sortable)
                propertyExpression,                     // Property expression for data access
                aggregatetable                          // Whether this column should be aggregated
        );
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getPropertyExpression() {
        return propertyExpression;
    }
}