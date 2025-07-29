package com.monbat.pages.salesOrders;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.components.genericTable.DynamicColumnDefinition;
import com.monbat.components.genericTable.GroupedColumnDefinition;
import com.monbat.components.genericTable.PropertyColumnDefinition;
import com.monbat.models.dto.sap.sales_order.SalesOrderMain;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.util.*;
import java.util.function.Function;

/**
 * Enhanced table panel that supports grouped columns for dynamic sales order data
 */
public class EnhancedSalesOrderTablePanel extends Panel {

    public EnhancedSalesOrderTablePanel(String id,
                                        IModel<Collection<SalesOrderMain>> model,
                                        Set<String> salesOrderNumbers,
                                        Function<SalesOrderMain, List<String>> filterFunction) {
        super(id);
        setOutputMarkupId(true);

        // Create all columns (standard + dynamic)
        List<IColumn<SalesOrderMain, String>> allColumns = createAllColumns(salesOrderNumbers);

        // Create data provider
        SortableDataProvider<SalesOrderMain, String> dataProvider = createDataProvider(model);

        // Create the data table
        DataTable<SalesOrderMain, String> dataTable = new DefaultDataTable<>(
                "dataTable",
                allColumns,
                dataProvider,
                10
        );

        dataTable.setOutputMarkupId(true);
        add(dataTable);

        // Add custom header if needed for grouped columns
        addGroupedHeaderSupport(salesOrderNumbers);
    }

    private List<IColumn<SalesOrderMain, String>> createAllColumns(Set<String> salesOrderNumbers) {
        List<IColumn<SalesOrderMain, String>> columns = new ArrayList<>();

        // Add standard columns
        columns.addAll(createStandardColumns());

        // Add dynamic columns for each sales order
        for (String salesOrderNumber : salesOrderNumbers) {
            columns.add(new DynamicColumnDefinition(salesOrderNumber, "quantity").createColumn());
            columns.add(new DynamicColumnDefinition(salesOrderNumber, "plannedOrder").createColumn());
            columns.add(new DynamicColumnDefinition(salesOrderNumber, "productionOrder").createColumn());
        }

        return columns;
    }

    private List<IColumn<SalesOrderMain, String>> createStandardColumns() {
        List<IColumn<SalesOrderMain, String>> columns = new ArrayList<>();

        // Add your standard columns here
        columns.add(new PropertyColumnDefinition<SalesOrderMain>("Material", "material").createColumn());
        columns.add(new PropertyColumnDefinition<SalesOrderMain>("Requested Quantity", "requestedQuantity", true, true).createColumn());
        columns.add(new PropertyColumnDefinition<SalesOrderMain>("Plant", "plant").createColumn());
        columns.add(new PropertyColumnDefinition<SalesOrderMain>("Unit", "requestedQuantityUnit").createColumn());
        columns.add(new PropertyColumnDefinition<SalesOrderMain>("Available Not Charged", "availableNotCharged", true, true).createColumn());
        columns.add(new PropertyColumnDefinition<SalesOrderMain>("Available Charged", "availableCharged", true, true).createColumn());

        return columns;
    }

    private SortableDataProvider<SalesOrderMain, String> createDataProvider(IModel<Collection<SalesOrderMain>> model) {
        return new SortableDataProvider<SalesOrderMain, String>() {
            @Override
            public Iterator<? extends SalesOrderMain> iterator(long first, long count) {
                List<SalesOrderMain> data = new ArrayList<>(model.getObject());
                // Add sorting logic here if needed
                return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
            }

            @Override
            public long size() {
                return model.getObject().size();
            }

            @Override
            public IModel<SalesOrderMain> model(SalesOrderMain object) {
                return () -> object;
            }
        };
    }

    /**
     * Add support for grouped headers using CSS and JavaScript
     */
    private void addGroupedHeaderSupport(Set<String> salesOrderNumbers) {
        // This would require custom CSS and possibly JavaScript to create
        // visual grouping of the headers. For now, we'll use the column names
        // to indicate the grouping.

        // You could add custom CSS classes or JavaScript behavior here
        // to style the grouped headers appropriately
    }

    /**
     * Create grouped column definitions for better organization
     */
    public static List<GroupedColumnDefinition<SalesOrderMain>> createGroupedColumns(Set<String> salesOrderNumbers) {
        List<GroupedColumnDefinition<SalesOrderMain>> groupedColumns = new ArrayList<>();

        // Standard columns group
        List<ColumnDefinition<SalesOrderMain>> standardColumns = Arrays.asList(
                new PropertyColumnDefinition<>("Material", "material"),
                new PropertyColumnDefinition<>("Requested Quantity", "requestedQuantity", true, true),
                new PropertyColumnDefinition<>("Plant", "plant"),
                new PropertyColumnDefinition<>("Unit", "requestedQuantityUnit"),
                new PropertyColumnDefinition<>("Available Not Charged", "availableNotCharged", true, true),
                new PropertyColumnDefinition<>("Available Charged", "availableCharged", true, true)
        );
        groupedColumns.add(new GroupedColumnDefinition<>("Standard Info", standardColumns));

        // Dynamic columns for each sales order
        for (String salesOrderNumber : salesOrderNumbers) {
            List<ColumnDefinition<SalesOrderMain>> dynamicColumns = Arrays.asList(
                    new DynamicColumnDefinition(salesOrderNumber, "quantity"),
                    new DynamicColumnDefinition(salesOrderNumber, "plannedOrder"),
                    new DynamicColumnDefinition(salesOrderNumber, "productionOrder")
            );
            groupedColumns.add(new GroupedColumnDefinition<>("SO " + salesOrderNumber, dynamicColumns));
        }

        return groupedColumns;
    }
}