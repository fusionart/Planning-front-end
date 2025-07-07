package com.monbat.models.dto.sap.sales_order;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SalesOrder implements Serializable {
    private String salesOrderNumber;
    private String soldToParty;
    private LocalDateTime requestedDeliveryDate;
    private String requestedDeliveryWeek;
    private Boolean completeDelivery;
    private List<SalesOrderItem> toItem;
}