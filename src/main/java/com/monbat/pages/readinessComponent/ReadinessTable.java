package com.monbat.pages.readinessComponent;

import com.monbat.components.genericTable.ColumnDefinition;
import com.monbat.components.genericTable.GenericDataTablePanel;
import com.monbat.components.genericTable.PropertyColumnDefinition;
import com.monbat.models.dto.ReadinessDetailWithDate;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ReadinessTable extends Panel {
    public ReadinessTable(String id, IModel<Collection<ReadinessDetailWithDate>> model) {
        super(id);
        setOutputMarkupId(true);

        // Define columns
        List<ColumnDefinition<ReadinessDetailWithDate>> columns = Arrays.asList(
                // Checkbox column would need a custom column definition
                new PropertyColumnDefinition<>("Date", "date", true, false),
                new PropertyColumnDefinition<>("Prod. Plant", "detail.productionPlant"),
                new PropertyColumnDefinition<>("Sales Document", "detail.salesDocument", true, false),
                new PropertyColumnDefinition<>("Customer Name", "detail.customerName", true, false),
                new PropertyColumnDefinition<>("Material", "detail.material", true, true),
                new PropertyColumnDefinition<>("Order Quantity", "detail.orderQuantity", true, true),
                new PropertyColumnDefinition<>("Work Center", "detail.workCenter", true, false),
                new PropertyColumnDefinition<>("Avail Qty 11", "availableQuantity11"),
                new PropertyColumnDefinition<>("Avail Qty 20", "availableQuantity20")
        );

        GenericDataTablePanel<ReadinessDetailWithDate> dataTablePanel = getComponents(model, columns);

        add(dataTablePanel);
    }

    private static GenericDataTablePanel<ReadinessDetailWithDate> getComponents(IModel<Collection<ReadinessDetailWithDate>> model, List<ColumnDefinition<ReadinessDetailWithDate>> columns) {
        Function<ReadinessDetailWithDate, List<String>> filterFunc =
                (Function<ReadinessDetailWithDate, List<String>> & Serializable) detail -> Arrays.asList(
                        detail.getDate() != null ? detail.getDate().toString() : "",
                        detail.getDetail() != null ? String.valueOf(detail.getDetail().getProductionPlant()) : "",
                        detail.getDetail() != null ? String.valueOf(detail.getDetail().getSalesDocument()) : "",
                        detail.getDetail() != null ? detail.getDetail().getCustomerName() : "",
                        detail.getDetail() != null ? detail.getDetail().getMaterial() : "",
                        detail.getDetail() != null ? String.valueOf(detail.getDetail().getOrderQuantity()) : "",
                        detail.getDetail() != null ? detail.getDetail().getWorkCenter() : "",
                        String.valueOf(detail.getAvailableQuantity11()),
                        String.valueOf(detail.getAvailableQuantity20())
                );

//        return new GenericDataTablePanel<>(
//                "table",
//                model,
//                columns,
//                filterFunc
//        );
        return null;
    }
}