package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.Client;
import ru.practicum.dto.*;
import ru.practicum.dto.dto.ViewStatsDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final Client statClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<EventState> eventStates = null;
        if (states != null) {
            try {
                eventStates = states.stream()
                        .map(EventState::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new ConflictException("Недопустимое значение статуса события");
            }
        }

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : null;

        Specification<Event> spec = EventSpecifications.byAdminFilters(users, eventStates, categories, start, end);

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);

                    long confirmed = confirmedRequests.getOrDefault(event.getId(), 0L);
                    dto.setConfirmedRequests((int) confirmed);

                    if (event.getPublishedOn() != null) {
                        List<ViewStatsDto> stats = statClient.getStats(
                                event.getPublishedOn().format(formatter),
                                LocalDateTime.now().format(formatter),
                                List.of("/events/" + event.getId()),
                                true
                        );

                        long views = stats.stream()
                                .filter(s -> ("/events/" + event.getId()).equals(s.getUri()))
                                .findFirst()
                                .map(ViewStatsDto::getHits)
                                .orElse(0L);

                        dto.setViews((int) views);
                    } else {
                        dto.setViews(0);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Событие не в состоянии ожидания публикации");
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Дата начала события должна быть не ранее чем через час от публикации");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(request.getEventDate(), formatter);
            if (newEventDate.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Дата события не может быть в прошлом");
            }
            event.setEventDate(newEventDate);
        }
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        event.setConfirmedRequests((int)requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED));

        if (event.getPublishedOn() != null) {
            List<ViewStatsDto> stats = statClient.getStats(event.getPublishedOn().format(formatter),
                    LocalDateTime.now().format(formatter),
                    List.of("/events/" + eventId),
                    true);
            event.setViews(stats.isEmpty() ? 0 : Math.toIntExact(stats.getFirst().getHits()));
        } else {
            event.setViews(0);
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + request.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        return eventMapper.toFullDto(eventRepository.save(event));
    }

    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);

                    long confirmed = confirmedRequests.getOrDefault(event.getId(), 0L);
                    dto.setConfirmedRequests((int) confirmed);

                    if (event.getPublishedOn() != null) {
                        List<ViewStatsDto> stats = statClient.getStats(
                                event.getPublishedOn().format(formatter),
                                LocalDateTime.now().format(formatter),
                                List.of("/events/" + event.getId()),
                                true
                        );

                        long views = stats.stream()
                                .filter(s -> ("/events/" + event.getId()).equals(s.getUri()))
                                .findFirst()
                                .map(ViewStatsDto::getHits)
                                .orElse(0L);

                        dto.setViews((int) views);
                    } else {
                        dto.setViews(0);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }


    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id" + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id " + newEventDto.getCategory() + " не найдена"));

        Event event = eventMapper.toEntity(newEventDto, category, eventDate);
        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        return eventMapper.toFullDto(eventRepository.save(event));
    }


    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " и пользователем id " + userId + " не найдено"));
        event.setConfirmedRequests((int)requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED));

        if (event.getPublishedOn() != null) {
            List<ViewStatsDto> stats = statClient.getStats(event.getPublishedOn().format(formatter),
                    LocalDateTime.now().format(formatter),
                    List.of("/events/" + eventId),
                    true);
            event.setViews(stats.isEmpty() ? 0 : Math.toIntExact(stats.getFirst().getHits()));
        } else {
            event.setViews(0);
        }

        return eventMapper.toFullDto(event);
    }

    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Могут быть обновлены только отложенные или отмененные события");
        }

        if (updateDto.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(updateDto.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Дата проведения мероприятия должна быть назначена не позднее, чем через 2 часа");
            }
            event.setEventDate(newDate);
        }

        if (Objects.equals(updateDto.getStateAction(), "SEND_TO_REVIEW")) {
            event.setState(EventState.PENDING);
        } else {
            event.setState(EventState.CANCELED);
        }

        if (updateDto.getAnnotation() != null) event.setAnnotation(updateDto.getAnnotation());
        if (updateDto.getDescription() != null) event.setDescription(updateDto.getDescription());
        if (updateDto.getTitle() != null) event.setTitle(updateDto.getTitle());
        if (updateDto.getPaid() != null) event.setPaid(updateDto.getPaid());
        if (updateDto.getParticipantLimit() != null) event.setParticipantLimit(updateDto.getParticipantLimit());
        if (updateDto.getRequestModeration() != null) event.setRequestModeration(updateDto.getRequestModeration());
        if (updateDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + updateDto.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        event.setConfirmedRequests((int)requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED));
        if (event.getPublishedOn() != null) {
            List<ViewStatsDto> stats = statClient.getStats(event.getPublishedOn().format(formatter),
                    LocalDateTime.now().format(formatter),
                    List.of("/events/" + eventId),
                    true);
            event.setViews(stats.isEmpty() ? 0 : Math.toIntExact(stats.getFirst().getHits()));
        } else {
            event.setViews(0);
        }

        return eventMapper.toFullDto(eventRepository.save(event));
    }

    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
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

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);

                    long confirmed = confirmedRequests.getOrDefault(event.getId(), 0L);
                    dto.setConfirmedRequests((int) confirmed);

                    if (event.getPublishedOn() != null) {
                        List<ViewStatsDto> stats = statClient.getStats(
                                event.getPublishedOn().format(formatter),
                                LocalDateTime.now().format(formatter),
                                List.of("/events/" + event.getId()),
                                true
                        );

                        long views = stats.stream()
                                .filter(s -> ("/events/" + event.getId()).equals(s.getUri()))
                                .findFirst()
                                .map(ViewStatsDto::getHits)
                                .orElse(0L);

                        dto.setViews((int) views);
                    } else {
                        dto.setViews(0);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public EventFullDto getEventPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не опубликовано"));

        event.setConfirmedRequests((int)requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED));

        if (event.getPublishedOn() != null) {
            List<ViewStatsDto> stats = statClient.getStats(event.getPublishedOn().format(formatter),
                    LocalDateTime.now().format(formatter),
                    List.of("/events/" + eventId),
                    true);
            event.setViews(stats.isEmpty() ? 0 : Math.toIntExact(stats.getFirst().getHits()));
        } else {
            event.setViews(0);
        }

        return eventMapper.toFullDto(event);
    }

    private Sort getSort(String sort) {
        if ("VIEWS".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "views");
        }
        return Sort.by(Sort.Direction.ASC, "eventDate");
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        return requestRepository.countConfirmedRequests(eventIds).stream()
                .collect(Collectors.toMap(
                        EventRequestCount::getEventId,
                        EventRequestCount::getCount
                ));
    }

}
