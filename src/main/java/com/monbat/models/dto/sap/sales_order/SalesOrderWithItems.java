package com.monbat.models.dto.sap.sales_order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderWithItems {
    private String material;
    private Double requestedQuantity;
    private String requestedQuantityUnit;
    private String plant;
    private String salesOrderNumber;
    private String soldToParty;
    private LocalDateTime requestedDeliveryDate;
    private String requestedDeliveryWeek;
    private String sdProcessStatus;
    private Boolean completeDelivery;
    private String plannedOrder;
    private String productionOrder;
}
