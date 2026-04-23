package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.util.Collection;

public interface EventStorage {

    Collection<Event> getFeed(long userId);

    void addEvent(long userId, EventType eventType, EventOperation operation, long entityId);
}
