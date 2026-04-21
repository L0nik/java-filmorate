package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface EventStorage {

    Collection<Event> getFeed(long userId);

    void addEvent(long userId, String eventType, String operation, long entityId);
}
