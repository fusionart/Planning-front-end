package com.monbat.pages.readinessComponent;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import java.io.Serializable;

public abstract class AggregatingPropertyColumn<T, S> extends AbstractColumn<T, S> {
    private final boolean summable;
    private final boolean countable;

    public AggregatingPropertyColumn(IModel<String> displayModel, S sortProperty, boolean summable, boolean countable) {
        super(displayModel, sortProperty);
        this.summable = summable;
        this.countable = countable;
    }

    public boolean isSummable() {
        return summable;
    }

    public boolean isCountable() {
        return countable;
    }

    public abstract Object getData(T rowObject);

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new Label(componentId, (Serializable) getData(rowModel.getObject())));
    }

    public void populateFooterCell(Item<ICellPopulator<T>> item, String componentId, IDataProvider<T> dataProvider) {
        item.add(new Label(componentId, "Footer")); // Placeholder, handled in AggregateToolbar
    }
}

