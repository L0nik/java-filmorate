package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.RecommendationStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationStorage recommendationStorage;
    private final FilmStorage filmStorage;

    public List<Film> getRecommendations(Long userId) {

        Optional<Long> similarUserId =
                recommendationStorage.findMostSimilarUserId(userId);

        if (similarUserId.isEmpty()) {
            return List.of();
        }

        Set<Long> filmIds =
                recommendationStorage.getRecommendedFilmIds(
                        userId,
                        similarUserId.get()
                );

        return filmIds.stream()
                .map(filmStorage::getFilmById)
                .toList();
    }
}