package com.monbat.models.entities;

import com.monbat.models.dto.ReadinessDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TabData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private List<ReadinessDetail> content;
}
