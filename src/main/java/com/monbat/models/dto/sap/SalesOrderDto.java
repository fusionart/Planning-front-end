package com.monbat.models.dto.sap;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SalesOrderDto implements Serializable {
    private String salesOrderNumber;
    private String soldToParty;
    private LocalDate requestedDeliveryDate;
    private String requestedDeliveryWeek;
    private List<ToItem> toItem;
}
