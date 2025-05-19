package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.dto.EndpointHitDto;
import ru.practicum.dto.dto.ViewStatsDto;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Client {
  private final RestTemplate restTemplate;

  private final String statsServerUrl = "http://localhost:9090";

  public void hit(EndpointHitDto hitDto) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, headers);

    restTemplate.exchange(
      statsServerUrl + "/hit",
      HttpMethod.POST,
      request,
      Void.class
    );
  }

  public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
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

    return Arrays.asList(response.getBody());
  }
}
