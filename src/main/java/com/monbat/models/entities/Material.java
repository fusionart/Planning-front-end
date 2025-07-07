package com.monbat.models.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Material{
    private String material;
    private Integer plant;
    private String description;
    private String materialType;
    private String materialGroup;
    private String uom;
    private String externalMaterialGroup;
    private Integer leadTimeOffset;
    private Integer curringTime;
    private Double netWeight;
}
