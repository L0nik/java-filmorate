package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DbRecommendationStorage
        extends DbBaseStorage<Long>
        implements RecommendationStorage {

    private static final String GET_SIMILAR_USER =
            "SELECT fl2.user_id " +
                    "FROM film_likes AS fl1 " +
                    "JOIN film_likes AS fl2 " +
                    "ON fl1.user_id != fl2.user_id " +
                    "AND fl1.film_id = fl2.film_id " +
                    "WHERE fl1.user_id = ? " +
                    "GROUP BY fl2.user_id " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT 1";

    private static final String GET_RECOMMENDED_FILMS =
            "SELECT film_id " +
                    "FROM film_likes " +
                    "WHERE user_id = ? " +
                    "AND film_id NOT IN (" +
                    "    SELECT film_id " +
                    "    FROM film_likes " +
                    "    WHERE user_id = ?" +
                    ")";

    private static final RowMapper<Long> LONG_MAPPER =
            (rs, rowNum) -> rs.getLong(1);

    public DbRecommendationStorage(JdbcTemplate jdbc) {
        super(jdbc, LONG_MAPPER);
    }

    @Override
    public Optional<Long> findMostSimilarUserId(Long userId) {
        return findOne(GET_SIMILAR_USER, userId);
    }

    @Override
    public Set<Long> getRecommendedFilmIds(Long userId, Long similarUserId) {
        List<Long> list =
                findMany(GET_RECOMMENDED_FILMS, similarUserId, userId);

        return new HashSet<>(list);
    }
}