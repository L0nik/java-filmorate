package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private IdWrapper mpa;
    private Set<Long> likes;
    private Set<IdWrapper> genres;
}
