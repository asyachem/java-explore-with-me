package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class NewUserDto {
    @NotBlank
    @Size(min = 2, max = 250, message = "Длина названия должна не больше 250 символов и не меньше 2")
    private String name;

    @Email
    @NotBlank
    @Size(min = 6, max = 254, message = "Длина почты должна не больше 254 символов и не меньше 6")
    private String email;
}
