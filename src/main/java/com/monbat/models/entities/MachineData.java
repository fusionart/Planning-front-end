package com.monbat.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MachineData implements Serializable {
    private String grouping;
    private String machine;
    private String machineDescription;
    private Map<String, Integer> weeklyData;

    // Constructor, getters, and setters
}
