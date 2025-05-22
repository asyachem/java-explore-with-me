package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.dto.ViewStatsDto;
import ru.practicum.service.StatsService;
import ru.practicum.dto.dto.EndpointHitDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {
  private final StatsService statsService;

  @PostMapping("/hit")
  @ResponseStatus(HttpStatus.CREATED)
  public void saveHit(@RequestBody EndpointHitDto hitDto) {
    statsService.saveHit(hitDto);
  }

  @GetMapping("/stats")
  public List<ViewStatsDto> getStats(@RequestParam String start,
                                     @RequestParam String end,
                                     @RequestParam(required = false) List<String> uris,
                                     @RequestParam(defaultValue = "false") boolean unique) {
    return statsService.getStats(start, end, uris, unique);
  }
}
