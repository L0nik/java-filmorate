package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.Collection;

public interface FriendshipStorage {
    void addFriend(long userId, long friendId);
    void deleteFriend(long userId, long friendId);
    Collection<Friendship> getFriendsOfUser(long userId);
    boolean checkIfUserHasFriend(long userId, long friendId);
}
