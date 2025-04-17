package com.monbat.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GridDto implements Serializable {
    Map<String, List<GridDetailsDto>> map = new LinkedHashMap<>();

    public List<GridDetailsDto> get(String key) {
        return map.get(key);
    }

    public void put(String key, List<GridDetailsDto> value) {
        map.put(key, value);
    }
}
