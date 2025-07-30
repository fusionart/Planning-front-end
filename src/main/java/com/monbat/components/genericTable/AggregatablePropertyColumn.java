package com.monbat.components.genericTable;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;

import java.io.Serializable;

public class AggregatablePropertyColumn<T extends Serializable, S> extends PropertyColumn<T, S> {
    private final boolean aggregatable;
    private final String header;

    public AggregatablePropertyColumn(IModel<String> displayModel, S sortProperty, String propertyExpression, boolean aggregatable) {
        super(displayModel, sortProperty, propertyExpression);
        this.aggregatable = aggregatable;
        this.header = displayModel.getObject();
    }

    public AggregatablePropertyColumn(IModel<String> displayModel, String propertyExpression, boolean aggregatable) {
        super(displayModel, propertyExpression);
        this.aggregatable = aggregatable;
        this.header = displayModel.getObject();
    }

    public boolean isAggregatable() {
        return aggregatable;
    }

    public String getHeader() {
        return header;
    }
}