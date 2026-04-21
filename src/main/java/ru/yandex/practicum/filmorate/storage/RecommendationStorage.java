package ru.yandex.practicum.filmorate.storage;

import java.util.Optional;
import java.util.Set;

public interface RecommendationStorage {

    Optional<Long> findMostSimilarUserId(Long userId);

    Set<Long> getRecommendedFilmIds(Long userId, Long similarUserId);
}