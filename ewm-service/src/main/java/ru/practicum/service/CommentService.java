package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.enums.CommentStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<CommentDto> getPublishedComments(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED, pageable);
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    public CommentDto getPublishedCommentById(Long eventId, Long commentId) {
        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден у указанного события"));

        if (comment.getStatus() != CommentStatus.APPROVED) {
            throw new NotFoundException("Комментарий не опубликован");
        }

        return commentMapper.toDto(comment);
    }

    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Пользователь не является автором комментария");
        }

        comment.setText(newCommentDto.getText());
        comment.setStatus(CommentStatus.PENDING);
        comment.setUpdatedAt(LocalDateTime.now());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Пользователь не является автором комментария");
        }

        commentRepository.delete(comment);
    }

    public List<CommentDto> getUserComments(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Comment> comments = commentRepository.findAllByAuthorIdOrderByCreatedAtDesc(userId);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getPendingComments() {
        List<Comment> comments = commentRepository.findAllByStatus(CommentStatus.PENDING);
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    public CommentDto approveComment(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Комментарий уже обработан");
        }

        comment.setStatus(CommentStatus.APPROVED);
        comment.setUpdatedAt(LocalDateTime.now());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    public CommentDto rejectComment(Long commentId, String reason) {
        Comment comment = getCommentOrThrow(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Комментарий уже обработан");
        }

        comment.setStatus(CommentStatus.REJECTED);
        comment.setRejectionReason(reason);
        comment.setUpdatedAt(LocalDateTime.now());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    private Comment getCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + id + " не найден"));
    }
}
