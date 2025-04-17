package com.monbat.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TabData<T> implements Serializable {
    private String title;
    private List<T> content;
}
