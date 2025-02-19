package com.monbat.models.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReadinessByDate {
    private final Map<Date, List<ReadinessDetail>> readinessByDate = new HashMap<>();

    public List<ReadinessDetail> get(Date key) {
        return readinessByDate.get(key);
    }

    public void put(Date key, List<ReadinessDetail> value) {
        readinessByDate.put(key, value);
    }

    public Map<Date, List<ReadinessDetail>> getMap(){
        return readinessByDate;
    }
}
