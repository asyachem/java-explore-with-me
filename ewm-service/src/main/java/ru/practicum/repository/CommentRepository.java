package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.CommentStatus;
import ru.practicum.model.Comment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndEventId(Long commentId, Long eventId);

    List<Comment> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);

    List<Comment> findAllByStatus(CommentStatus status);

    int countByEventIdAndStatus(Long eventId, CommentStatus status);

    @Query("SELECT c.event.id, COUNT(c) FROM Comment c WHERE c.status = 'APPROVED' AND c.event.id IN :eventIds GROUP BY c.event.id")
    Map<Long, Long> countApprovedByEventIds(@Param("eventIds") List<Long> eventIds);

}
