package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.Client;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.dto.EndpointHitDto;
import ru.practicum.dto.dto.ViewStatsDto;
import ru.practicum.enums.EventState;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.EventSpecifications;
import ru.practicum.exception.BadRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicEventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Client statClient;

    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                         String sort, int from, int size, HttpServletRequest request) {

        Pageable pageable = PageRequest.of(from / size, size, getSort(sort));

        LocalDateTime start = rangeStart != null
                ? LocalDateTime.parse(rangeStart, formatter)
                : LocalDateTime.now();

        LocalDateTime end = null;
        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, formatter);

            if (start.isAfter(end)) {
                throw new BadRequestException("Дата начала не может быть позже даты конца");
            }
        }

        Specification<Event> spec = EventSpecifications.publicEvents(text, categories, paid, start, end, onlyAvailable);


        return eventRepository.findAll(spec, pageable).stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не опубликовано"));

        getViews(event);

        EndpointHitDto hit = EndpointHitDto.builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        statClient.hit(hit);

        return eventMapper.toFullDto(event);
    }

    private Sort getSort(String sort) {
        if ("VIEWS".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "views");
        }
        return Sort.by(Sort.Direction.ASC, "eventDate");
    }

    private void getViews(Event event) {
        List<String> uris = new ArrayList<>();
        uris.add("/events/" + event.getId());

        String start = "2000-01-01 00:00:00";
        String end = event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<ViewStatsDto> dto = statClient.getStats(start, end, uris, true);

        if (!dto.isEmpty()) {
            event.setViews(Math.toIntExact(dto.getFirst().getHits()));
        }
    }
}
