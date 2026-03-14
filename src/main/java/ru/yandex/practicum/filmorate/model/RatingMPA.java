package ru.yandex.practicum.filmorate.model;

public enum RatingMPA {
    G,
    PG,
    PG_13,
    R,
    NC_17;

    @Override
    public String toString() {
        return switch (this) {
            case G -> "G";
            case PG -> "PG";
            case PG_13 -> "PG-13";
            case R -> "R";
            case NC_17 -> "NC-17";
        };
    }
}
