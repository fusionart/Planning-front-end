package com.monbat.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class  ReadinessDetailWithDate implements Serializable {
    private Date date;
    private ReadinessDetail detail;
    private Integer availableQuantity11;
    private Integer availableQuantity20;
}
