package ru.yandex.practicum.filmorate.model;

public class Friendship extends BaseModel {
    private Long requesterId;
    private Long addresseeId;
    private FriendshipStatus status;
}
