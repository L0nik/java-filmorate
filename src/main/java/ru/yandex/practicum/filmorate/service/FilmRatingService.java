package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmRatingStorage;

import java.util.Collection;

@Service
public class FilmRatingService {
    private final FilmRatingStorage ratingStorage;

    public FilmRatingService(FilmRatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Collection<FilmRating> getAllRatings() {
        return ratingStorage.getAllRatings();
    }

    public FilmRating getRatingById(long id) {
        return ratingStorage.getRatingById(id);
    }
}
