package com.monbat.components.genericTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicColumnFactory {
    public static List<DynamicColumnDefinition> createSalesOrderColumns(String salesOrderNumber) {
        List<DynamicColumnDefinition> columns = new ArrayList<>();

        // Quantity column - aggregatable
        columns.add(new DynamicColumnDefinition(salesOrderNumber, "quantity", true));

        // Text columns - not aggregatable
        columns.add(new DynamicColumnDefinition(salesOrderNumber, "plannedOrder", false));
        columns.add(new DynamicColumnDefinition(salesOrderNumber, "productionOrder", false));

        return columns;
    }

    public static List<DynamicColumnDefinition> createQuantityColumns(Set<String> salesOrderNumbers) {
        return salesOrderNumbers.stream()
                .map(soNumber -> new DynamicColumnDefinition(soNumber, "quantity", true))
                .collect(Collectors.toList());
    }

    public static List<DynamicColumnDefinition> createAllColumns(Set<String> salesOrderNumbers) {
        return salesOrderNumbers.stream()
                .flatMap(soNumber -> createSalesOrderColumns(soNumber).stream())
                .collect(Collectors.toList());
    }
}
