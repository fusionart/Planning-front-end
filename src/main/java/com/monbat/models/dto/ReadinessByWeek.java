package com.monbat.models.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadinessByWeek {
    private final Map<String, List<ReadinessByDate>> readinessByWeek = new HashMap<>();

    public List<ReadinessByDate> get(String key) {
        return readinessByWeek.get(key);
    }

    public void put(String key, List<ReadinessByDate> value) {
        readinessByWeek.put(key, value);
    }

    public Map<String, List<ReadinessByDate>> getMap(){
        return readinessByWeek;
    }
}
