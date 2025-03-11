package com.monbat.models.dto;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ReadinessByWeek implements Serializable {
    private final Map<String, List<ReadinessDetailWithDate>> map = new HashMap<>();

    public List<ReadinessDetailWithDate> get(String key) {
        return map.get(key);
    }

    public void put(String key, List<ReadinessDetailWithDate> value) {
        map.put(key, value);
    }

}
