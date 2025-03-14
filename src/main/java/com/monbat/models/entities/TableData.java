package com.monbat.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableData implements Serializable {
    private String rowLabel;
    private Map<String, Map<Integer, CellData>> cellData;

}

