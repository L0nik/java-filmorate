package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Film extends BaseModel {

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotNull(message = "Отсутствует описание фильма")
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Не указана дата релиза")
    private LocalDate releaseDate;

    @NotNull(message = "Не указана продолжительность фильма")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    private FilmRating mpa;
    private Collection<Director> directors = new ArrayList<>();
    private final Set<Long> likes = new HashSet<>();
    private final Set<Genre> genres = new LinkedHashSet<>();

    @Getter
    private static final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @AssertTrue(message = "Дата релиза — не раньше 28.12.1895")
    public boolean isValidReleaseDate() {
        return !(releaseDate != null && releaseDate.isBefore(minReleaseDate));
    }

    public void putLike(long userId) {
        likes.add(userId);
    }

    public void removeLike(long userId) {
        likes.remove(userId);
    }

    public void addLikes(Collection<Long> likes) {
        this.likes.addAll(likes);
    }

    public void addGenres(Collection<Genre> genres) {
        this.genres.addAll(genres);
    }
}
