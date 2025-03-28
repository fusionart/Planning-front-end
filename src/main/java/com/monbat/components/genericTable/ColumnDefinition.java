package com.monbat.components.genericTable;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

import java.io.Serializable;

public interface ColumnDefinition<T extends Serializable> {
    /**
     * Create an IColumn for the given type
     * @return IColumn instance
     */
    IColumn<T, String> createColumn();

    /**
     * Get the header text for the column
     * @return Column header
     */
    String getHeader();

    /**
     * Get the property expression for sorting and filtering
     * @return Property expression
     */
    String getPropertyExpression();
}