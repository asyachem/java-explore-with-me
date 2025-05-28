package ru.practicum.dto;

import lombok.Data;
import ru.practicum.model.Location;
import ru.practicum.enums.EventState;

@Data
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventState state;
    private String createdOn;
    private String publishedOn;
    private UserShortDto initiator;
    private CategoryDto category;
    private int views;
    private int confirmedRequests;
}
