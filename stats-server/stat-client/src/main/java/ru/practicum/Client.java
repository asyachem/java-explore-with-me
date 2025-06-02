package ru.practicum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.dto.EndpointHitDto;
import ru.practicum.dto.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class Client {
  private final RestTemplate restTemplate;

  @Value("${stats.server.url}")
  private String statsServerUrl;

  @Value("${app.name}")
  private String appName;

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public void hit(HttpServletRequest request) {
    EndpointHitDto hit = new EndpointHitDto(
            appName,
            request.getRequestURI(),
            request.getRemoteAddr(),
            LocalDateTime.now().format(formatter)
    );
    sendHit(hit);
  }

  public void sendHit(EndpointHitDto hitDto) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, headers);

    try {
      restTemplate.exchange(
              statsServerUrl + "/hit",
              HttpMethod.POST,
              request,
              Void.class
      );
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      log.error("Ошибка при отправке запроса /hit: статус={}, тело={}", e.getStatusCode(), e.getResponseBodyAsString());
    } catch (ResourceAccessException ex) {
      log.error("Ошибка доступа к сервису статистики: {}", ex.getMessage());
    } catch (Exception ex) {
      log.error("Неизвестная ошибка при отправке запроса /hit", ex);
    }
  }

  public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
    try {
      UriComponentsBuilder uriBuilder = UriComponentsBuilder
              .fromHttpUrl(statsServerUrl + "/stats")
              .queryParam("start", start)
              .queryParam("end", end)
              .queryParam("unique", unique);

      if (uris != null && !uris.isEmpty()) {
        for (String uri : uris) {
          uriBuilder.queryParam("uris", uri);
        }
      }

      ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(
              uriBuilder.toUriString(),
              ViewStatsDto[].class
      );

      return Optional.ofNullable(response.getBody())
              .map(Arrays::asList)
              .orElse(Collections.emptyList());
    } catch (HttpClientErrorException | HttpServerErrorException ex) {
      log.error("Ошибка при получении статистики: статус={}, тело={}", ex.getStatusCode(), ex.getResponseBodyAsString());
    } catch (ResourceAccessException ex) {
      log.error("Ошибка доступа к сервису статистики: {}", ex.getMessage());
    } catch (Exception ex) {
      log.error("Неизвестная ошибка при получении статистики", ex);
    }

    return Collections.emptyList();
  }
}
