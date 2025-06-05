package ru.practicum.dto;

import lombok.Data;

@Data
public class EventShortDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private int confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private int views;
    private Integer commentsCount;
}
