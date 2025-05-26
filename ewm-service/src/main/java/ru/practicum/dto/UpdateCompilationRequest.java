package ru.practicum.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    private List<Long> events;

    @Size(max = 50, message = "Длина названия должна не больше 50 символов")
    private String title;

    private Boolean pinned;
}
