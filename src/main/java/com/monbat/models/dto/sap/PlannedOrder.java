package com.monbat.models.dto.sap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class PlannedOrder implements Serializable {
    private String plannedOrder;
    private String material;
    private String productionPlan;
    private Double totalQuantity;
    private String salesOrder;
}
