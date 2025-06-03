package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    // Получить список комментариев на модерации
    @GetMapping("/pending")
    public List<CommentDto> getPendingComments() {
        return commentService.getPendingComments();
    }

    // Одобрить комментарий
    @PatchMapping("/{id}/approve")
    public CommentDto approve(@PathVariable Long id) {
        return commentService.approveComment(id);
    }

    // Отклонить комментарий (с причиной)
    @PatchMapping("/{id}/reject")
    public CommentDto reject(@PathVariable Long id, @RequestParam String reason) {
        return commentService.rejectComment(id, reason);
    }
}
