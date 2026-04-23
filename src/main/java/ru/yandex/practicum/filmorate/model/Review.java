package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class Review {

    Long reviewId;

    @NotBlank(message = "В отзыве отсутствует контент")
    String content;

    @NotNull(message = "Не указан тип отзыва (положительный/отрицательный)")
    Boolean isPositive;

    @NotNull(message = "Не указан id пользователя, оставившего отзыв")
    Long userId;

    @NotNull(message = "Не указан id фильма, к которому относится отзыв")
    Long filmId;

    int useful;
}
