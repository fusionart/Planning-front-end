package com.monbat.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class  ReadinessDetailWithDate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Date date;
    private ReadinessDetail detail;
}
