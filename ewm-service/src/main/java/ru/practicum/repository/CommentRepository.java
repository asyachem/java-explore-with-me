package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.CommentStatus;
import ru.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndEventId(Long commentId, Long eventId);

    List<Comment> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);

    List<Comment> findAllByStatus(CommentStatus status);
}
