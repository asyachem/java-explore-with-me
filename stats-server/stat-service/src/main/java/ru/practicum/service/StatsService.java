package ru.practicum.service;
import java.net.URLDecoder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.dto.EndpointHitDto;
import ru.practicum.dto.dto.ViewStatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatsService {
  private final StatsRepository statsRepository;

  @Transactional
  public void saveHit(EndpointHitDto hitDto) {
    statsRepository.save(EndpointHitMapper.toEndpointHit(hitDto));
  }

  public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
    LocalDateTime startDate = (start != null)
            ? parseDateTime(start)
            : LocalDateTime.of(2000, 1, 1, 0, 0); // взяла самую позднюю дату

    LocalDateTime endDate = (end != null)
            ? parseDateTime(end)
            : LocalDateTime.now();

    if (startDate.isAfter(endDate)) {
      throw new BadRequestException("Дата начала не может быть позже даты конца");
    }

    List<ViewStatsDto> dtos;

    if (Objects.equals(uris.getFirst(), "/events")) {
      dtos = statsRepository.getAllStatsForEventsWithoutTime();
    } else if (uris.size() == 1 && "/events".equals(uris.get(0))) {
      dtos = unique
              ? statsRepository.getStatsUniqueForAllEvents(startDate, endDate)
              : statsRepository.getStatsForAllEvents(startDate, endDate);
    } else {
      dtos = unique
              ? statsRepository.getStatsUnique(startDate, endDate, uris)
              : statsRepository.getStats(startDate, endDate, uris);
    }

    return dtos;
  }

  private LocalDateTime parseDateTime(String dateTime) {
    try {
      String decoded = URLDecoder.decode(dateTime, StandardCharsets.UTF_8);
      return LocalDateTime.parse(decoded, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } catch (DateTimeParseException e) {
      throw new BadRequestException("Неверный формат даты. Используйте формат 'yyyy-MM-dd HH:mm:ss'");
    }
  }
}
