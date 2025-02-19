package com.monbat.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadinessDetail implements Serializable {
    private Integer salesDocument;
    private String soldToParty;
    private String customerName;
    private String reqDlvWeek;
    private String material;
    private Integer orderQuantity;
    private Integer productionPlant;
    private String batteryType;
    private String workCenter;
}
