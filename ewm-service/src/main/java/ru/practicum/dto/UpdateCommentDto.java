package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommentDto {
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;
}
