package com.monbat.components.genericTable;

import lombok.Getter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

import java.io.Serializable;
import java.util.List;


@Getter
public class GroupedColumnDefinition<T extends Serializable> implements Serializable {
    private final String mainHeader;
    private final List<ColumnDefinition<T>> subColumns;

    public GroupedColumnDefinition(String mainHeader, List<ColumnDefinition<T>> subColumns) {
        this.mainHeader = mainHeader;
        this.subColumns = subColumns;
    }

    /**
     * Get all individual columns for table creation
     */
    public List<IColumn<T, String>> createColumns() {
        return subColumns.stream()
                .map(ColumnDefinition::createColumn)
                .toList();
    }
}