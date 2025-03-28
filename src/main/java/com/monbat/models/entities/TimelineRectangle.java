package com.monbat.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimelineRectangle implements Serializable {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String title;
    private String color;
}
