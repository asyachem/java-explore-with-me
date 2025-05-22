package ru.practicum.mapper;

import ru.practicum.dto.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EndpointHitMapper {
  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static EndpointHit toEndpointHit(EndpointHitDto hitDto) {
    return EndpointHit.builder()
      .app(hitDto.getApp())
      .uri(hitDto.getUri())
      .ip(hitDto.getIp())
      .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), formatter))
      .build();
  }
}
