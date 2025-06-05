package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.enums.CommentStatus;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "authorName", source = "author.name")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(NewCommentDto dto);

    default Comment fromDtoWithContext(NewCommentDto dto, User author, Event event) {
        Comment comment = toEntity(dto);
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(null);
        return comment;
    }
}
