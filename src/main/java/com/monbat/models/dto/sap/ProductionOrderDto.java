package com.monbat.models.dto.sap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductionOrderDto implements Serializable {
    private String material;
    private String materialDescription;
    private String productionOrder;
    private String productionPlant;
    private Boolean orderIsReleased;
    private Boolean orderIsScheduled;
    private String productionSupervisor;
    private String productionVersion;
    private String workCenter;
    private String workCenterDescription;
    private LocalDate mfgOrderScheduledStartDate;
    private LocalTime mfgOrderScheduledStartTime;
    private LocalDate mfgOrderScheduledEndDate;
    private LocalTime mfgOrderScheduledEndTime;
    private String productionUnit;
    private Double totalQuantity;
    private Double mfgOrderConfirmedYieldQty;
}
