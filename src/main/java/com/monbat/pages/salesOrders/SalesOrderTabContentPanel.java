package com.monbat.pages.salesOrders;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.components.genericTable.GenericDataTablePanel;
import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrderMain;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Content panel for sales order tabs with dynamic column support
 */
public class SalesOrderTabContentPanel extends Panel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public SalesOrderTabContentPanel(String id,
                                     List<SalesOrder> salesOrders,
                                     List<PlannedOrder> plannedOrderList,
                                     List<ProductionOrder> productionOrderList,
                                     IModel<Collection<SalesOrderMain>> model,
                                     List<ColumnDefinition<SalesOrderMain>> columns,
                                     Function<SalesOrderMain, List<String>> filterFunction) {
        super(id);
        setOutputMarkupId(true);

        // Use the existing generic data table panel with dynamic columns
        GenericDataTablePanel<SalesOrderMain> dataTablePanel = new GenericDataTablePanel<>(
                "content",
                model,
                columns,
                filterFunction
        );

        add(dataTablePanel);
    }
}