package com.monbat.models.entities;

import com.monbat.models.dto.ReadinessDetailWithDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TabData implements Serializable {
    private String title;
    private List<ReadinessDetailWithDate> content;
}
