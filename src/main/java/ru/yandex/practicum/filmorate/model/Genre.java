package ru.yandex.practicum.filmorate.model;

public enum Genre {
    COMEDY,
    DRAMA,
    CARTOON,
    THRILLER,
    DOCUMENTARY,
    ACTION_MOVIE;

    @Override
    public String toString() {
        return switch (this) {
            case COMEDY -> "Comedy";
            case DRAMA -> "Drama";
            case CARTOON -> "Cartoon";
            case THRILLER -> "Thriller";
            case DOCUMENTARY -> "Documentary";
            case ACTION_MOVIE -> "Action movie";
        };
    }
}
