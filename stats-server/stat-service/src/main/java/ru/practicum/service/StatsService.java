package ru.practicum.service;
import java.net.URLDecoder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.dto.EndpointHitDto;
import ru.practicum.dto.dto.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
  private final StatsRepository statsRepository;

  @Transactional
  public void saveHit(EndpointHitDto hitDto) {
    statsRepository.save(EndpointHitMapper.toEndpointHit(hitDto));
  }

  public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
    LocalDateTime startDate = parseDateTime(start);
    LocalDateTime endDate = parseDateTime(end);

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Дата начала не может быть позже даты конца");
    }

    if (unique) {
      return statsRepository.getStatsUnique(startDate, endDate, uris);
    } else {
      return statsRepository.getStats(startDate, endDate, uris);
    }
  }

  private LocalDateTime parseDateTime(String dateTime) {
    try {
      String decoded = URLDecoder.decode(dateTime, StandardCharsets.UTF_8);
      return LocalDateTime.parse(decoded, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Неверный формат даты. Используйте формат 'yyyy-MM-dd HH:mm:ss'");
    }
  }
}
