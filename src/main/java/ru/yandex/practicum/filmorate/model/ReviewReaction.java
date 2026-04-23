package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReviewReaction extends BaseModel {
    Long reviewId;
    Long userId;
    boolean isUseful;
}
