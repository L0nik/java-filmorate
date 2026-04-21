package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Event {
    private Long timestamp;
    private Long userId;
    private String eventType;
    private String operation;
    private Long eventId;
    private Long entityId;
}
