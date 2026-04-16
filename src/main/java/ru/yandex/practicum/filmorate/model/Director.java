package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Director extends BaseModel {

    @NotNull(message = "Имя режиссёра не может быть null")
    @NotBlank(message = "Имя режиссёра не может быть пустым или состоять только из пробелов")
    String name;

    public Director(long id, String name) {
        this.setId(id);
        this.name = name;
    }

    public Director() {
    }
}
