package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.util.Collection;
import java.util.List;

@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users ORDER BY id";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_USER_QUERY = """
                                                    INSERT INTO users(email, login, name, birthday)
                                                    VALUES (?, ?, ?, ?)
                                                    """;
    private static final String UPDATE_USER_QUERY = """
                                                    UPDATE users
                                                    SET email = ?, login = ?, name = ?, birthday = ?
                                                    WHERE id = ?
                                                    """;
    private static final String ADD_FRIEND_QUERY = """
                                                   MERGE INTO friendships (user_id, friend_id)
                                                   KEY(user_id, friend_id)
                                                   VALUES (?, ?)
                                                   """;
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY = """
                                                     SELECT u.*
                                                     FROM users u
                                                     JOIN friendships f
                                                     ON f.friend_id = u.id
                                                     WHERE f.user_id = ?
                                                     ORDER BY u.id
                                                     """;
    private static final String FIND_COMMON_FRIENDS_QUERY = """
                                                            SELECT u.*
                                                            FROM users u
                                                            JOIN friendships f1
                                                            ON f1.friend_id = u.id AND f1.user_id = ?
                                                            JOIN friendships f2
                                                            ON f2.friend_id = u.id AND f2.user_id = ?
                                                            ORDER BY u.id
                                                            """;

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper userMapper) {
        super(jdbc, userMapper);
    }

    @Override
    public Collection<User> getAll() {
        return findMany(FIND_ALL_USERS_QUERY);
    }

    @Override
    public User create(User user) {
        String name = user.getName() == null || user.getName().isBlank() ? user.getLogin() : user.getName();
        long id = insert(INSERT_USER_QUERY,
                         user.getEmail(),
                         user.getLogin(),
                         name,
                         user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()));
        return getById(id);
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Ну указан id пользователя");
        }
        String name = user.getName() == null || user.getName().isBlank() ? user.getLogin() : user.getName();
        int rows = update(UPDATE_USER_QUERY,
                          user.getEmail(),
                          user.getLogin(),
                          name,
                          user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()),
                          user.getId());

        if (rows == 0) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        return getById(user.getId());
    }

    @Override
    public User getById(Long id) {
        return findOne(FIND_USER_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return jdbc.query(FIND_FRIENDS_QUERY, mapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return jdbc.query(FIND_COMMON_FRIENDS_QUERY, mapper, userId, otherId);
    }
}
