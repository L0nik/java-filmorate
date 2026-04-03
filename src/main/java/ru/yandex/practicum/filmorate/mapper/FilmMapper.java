package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.IdWrapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashSet;
import java.util.LinkedHashSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {
    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setRatingId(request.getMpa().getId());
        if (request.getGenres() != null) {
            film.addGenres(request.getGenres().stream().map(IdWrapper::getId).toList());
        }
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setReleaseDate(film.getReleaseDate());
        filmDto.setDuration(film.getDuration());
        filmDto.setMpa(new IdWrapper(film.getRatingId()));
        filmDto.setGenres(
                new LinkedHashSet<>(
                        film.getGenres().stream()
                                .sorted()
                                .map(IdWrapper::new)
                                .toList()
                )
        );
        filmDto.setLikes(new HashSet<>(film.getLikes()));
        return filmDto;
    }
}
