package com.monbat.models.dto.sap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ToItem implements Serializable {
    private String material;
    private Double requestedQuantity;
    private String requestedQuantityUnit;
    private String SDProcessStatus;
}
