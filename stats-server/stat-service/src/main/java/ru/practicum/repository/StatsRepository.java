package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
  @Query("SELECT new ru.practicum.dto.dto.ViewStatsDto(e.app, e.uri, COUNT(e)) " +
    "FROM EndpointHit e " +
    "WHERE e.timestamp BETWEEN :start AND :end " +
    "AND (:uris IS NULL OR e.uri IN :uris) " +
    "GROUP BY e.app, e.uri " +
    "ORDER BY COUNT(e) DESC")
  List<ViewStatsDto> getStats(@Param("start") LocalDateTime  start,
                              @Param("end") LocalDateTime end,
                              @Param("uris") List<String> uris);

  @Query("SELECT new ru.practicum.dto.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
    "FROM EndpointHit e " +
    "WHERE e.timestamp BETWEEN :start AND :end " +
    "AND (:uris IS NULL OR e.uri IN :uris) " +
    "GROUP BY e.app, e.uri " +
    "ORDER BY COUNT(DISTINCT e.ip) DESC")
  List<ViewStatsDto> getStatsUnique(@Param("start") LocalDateTime  start,
                                    @Param("end") LocalDateTime  end,
                                    @Param("uris") List<String> uris);

  @Query("SELECT new ru.practicum.dto.dto.ViewStatsDto(e.app, e.uri, COUNT(e)) " +
          "FROM EndpointHit e " +
          "WHERE e.timestamp BETWEEN :start AND :end " +
          "AND e.uri LIKE '/events%' " +
          "GROUP BY e.app, e.uri " +
          "ORDER BY COUNT(e) DESC")
  List<ViewStatsDto> getStatsForAllEvents(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

  @Query("SELECT new ru.practicum.dto.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
          "FROM EndpointHit e " +
          "WHERE e.timestamp BETWEEN :start AND :end " +
          "AND e.uri LIKE '/events%' " +
          "GROUP BY e.app, e.uri " +
          "ORDER BY COUNT(DISTINCT e.ip) DESC")
  List<ViewStatsDto> getStatsUniqueForAllEvents(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

  @Query("SELECT new ru.practicum.dto.dto.ViewStatsDto(e.app, e.uri, COUNT(e)) " +
          "FROM EndpointHit e " +
          "WHERE e.uri LIKE '/events%' " +
          "GROUP BY e.app, e.uri " +
          "ORDER BY COUNT(e) DESC")
  List<ViewStatsDto> getAllStatsForEventsWithoutTime();


}
