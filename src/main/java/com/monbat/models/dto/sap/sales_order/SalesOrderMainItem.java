package com.monbat.models.dto.sap.sales_order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderMainItem implements Serializable {
    private Double quantity;
    private String plannedOrder;
    private String productionOrder;
}
