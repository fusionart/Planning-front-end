package com.monbat.models.wrappers;


import com.monbat.models.dto.sap.PlannedOrder;
import com.monbat.models.dto.sap.ProductionOrder;
import com.monbat.models.dto.sap.sales_order.SalesOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CombinedResponse {
    private List<SalesOrder> salesOrders;
    private List<PlannedOrder> plannedOrders;
    private List<ProductionOrder> productionOrders;
}
