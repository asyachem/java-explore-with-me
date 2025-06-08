package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventFullDto toFullDto(Event event) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate() != null ? event.getEventDate().format(formatter) : null);
        dto.setCreatedOn(event.getCreatedOn() != null ? event.getCreatedOn().format(formatter) : null);
        dto.setPublishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(formatter) : null);
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState());
        dto.setViews(event.getViews());
        dto.setConfirmedRequests(event.getConfirmedRequests());

        if (event.getLocation() != null) {
            Location locationDto = new Location();
            locationDto.setId(event.getLocation().getId());
            locationDto.setLat(event.getLocation().getLat());
            locationDto.setLon(event.getLocation().getLon());
            dto.setLocation(locationDto);
        }

        if (event.getInitiator() != null) {
            dto.setInitiator(userMapper.toShortDto(event.getInitiator()));
        }

        if (event.getCategory() != null) {
            dto.setCategory(categoryMapper.toDto(event.getCategory()));
        }

        return dto;
    }

    public EventFullDto toFullDto(Event event, int commentsCount) {
        EventFullDto dto = toFullDto(event);
        dto.setCommentsCount(commentsCount);
        return dto;
    }

    public EventShortDto toShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setEventDate(event.getEventDate().format(formatter));
        dto.setPaid(event.getPaid());
        dto.setInitiator(userMapper.toShortDto(event.getInitiator()));
        dto.setCategory(categoryMapper.toDto(event.getCategory()));
        dto.setViews(event.getViews());
        dto.setConfirmedRequests(event.getConfirmedRequests());
        return dto;
    }

    public EventShortDto toShortDto(Event event, int commentsCount) {
        EventShortDto dto = toShortDto(event);
        dto.setCommentsCount(commentsCount);
        return dto;
    }

    public Event toEntity(NewEventDto newEventDto, Category category, LocalDateTime eventDate) {
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(eventDate);
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setCategory(category);
        event.setTitle(newEventDto.getTitle());
        event.setLocation(newEventDto.getLocation());
        return event;
    }
}
