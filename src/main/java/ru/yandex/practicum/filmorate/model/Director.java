package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Director extends BaseModel {

    @NotNull(message = "Имя режиссёра не может быть null")
    @NotBlank(message = "Имя режиссёра не может быть пустым или состоять только из пробелов")
    private String name;
}
