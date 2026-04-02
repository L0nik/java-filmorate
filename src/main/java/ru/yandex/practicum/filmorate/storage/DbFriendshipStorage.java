package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FriendshipRowMapper;

import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
public class DbFriendshipStorage extends DbBaseStorage<Friendship> implements FriendshipStorage {

    private final static String ADD_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private final static String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private final static String GET_FRIENDS_OF_USER_QUERY = "SELECT * FROM friendship WHERE user_id = ?";

    public DbFriendshipStorage(JdbcTemplate jdbc, FriendshipRowMapper friendshipRowMapper) {
        super(jdbc, friendshipRowMapper);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        update(ADD_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Collection<Friendship> getFriendsOfUser(long userId) {
        return findMany(GET_FRIENDS_OF_USER_QUERY, userId);
    }

}
