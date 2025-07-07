package com.monbat.models.dto.sap;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductionOrder implements Serializable {
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
    private String salesOrder;
}
