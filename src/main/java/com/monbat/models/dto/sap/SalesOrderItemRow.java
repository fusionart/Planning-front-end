package com.monbat.models.dto.sap;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class SalesOrderItemRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SalesOrderDto order;
    private final ToItem item;
    private final String salesOrderNumber;
    private final String soldToParty;
    private final LocalDate requestedDeliveryDate;
    private final String requestedDeliveryWeek;
    private final String material;
    private final String quantityWithUnit;
    private final String sDProcessStatus;
    private final String completeDelivery;

    public SalesOrderItemRow(SalesOrderDto order, ToItem item) {
        this.order = order;
        this.item = item;

        this.salesOrderNumber = order.getSalesOrderNumber();
        this.soldToParty = order.getSoldToParty();
        this.requestedDeliveryDate = order.getRequestedDeliveryDate();
        this.material = item != null ? item.getMaterial() : "N/A";
        this.quantityWithUnit = item != null ?
                item.getRequestedQuantity() + " " + item.getRequestedQuantityUnit() : "N/A";
        this.sDProcessStatus = item != null ? item.getSDProcessStatus() : "N/A";
        this.requestedDeliveryWeek = order.getRequestedDeliveryWeek();
        this.completeDelivery = order.getCompleteDelivery();
    }
}
