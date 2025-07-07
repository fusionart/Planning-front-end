package com.monbat.models.dto.sap.sales_order;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SalesOrderItem implements Serializable {
    private String material;
    private Double requestedQuantity;
    private String requestedQuantityUnit;
    private String sdProcessStatus;
}
