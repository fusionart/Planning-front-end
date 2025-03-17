package com.monbat.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatteryQuantity implements Serializable {
    private String batteryCode;
    private Integer quantity;
    private Integer productionPlant;
    private Integer storageLocation;
    private String batch;
}