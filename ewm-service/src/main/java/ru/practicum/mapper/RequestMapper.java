package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.Request;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "created", target = "created", qualifiedByName = "formatDateTime")
    ParticipationRequestDto toDto(Request request);

    @Named("formatDateTime")
    static String formatDateTime(LocalDateTime time) {
        return time != null ? FORMATTER.format(time) : null;
    }
}
