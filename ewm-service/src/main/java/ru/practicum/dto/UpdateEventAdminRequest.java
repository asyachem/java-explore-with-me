package ru.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.enums.StateAction;
import ru.practicum.model.Location;

@Data
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Длина аннотации должна не больше 2000 символов и не меньше 20")
    private String annotation;
    private Long category;

    @Size(min = 20, max = 7000, message = "Длина описания должна не больше 7000 символов и не меньше 20")
    private String description;
    private String eventDate; // дата и время события
    private Location location;
    private Boolean paid;

    @Min(value = 0, message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit; // лимит пользователей
    private Boolean requestModeration; // нужна ли пре-модерация заявок на участие
    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "Длина аннотации должна не больше 120 символов и не меньше 3")
    private String title;
}
