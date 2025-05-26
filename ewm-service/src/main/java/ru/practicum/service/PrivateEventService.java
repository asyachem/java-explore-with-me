package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.enums.EventState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable).stream()
                .map(eventMapper::toShortDto)
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

        return eventMapper.toFullDto(eventRepository.save(event));
    }
}
