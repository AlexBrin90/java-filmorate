package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {
    private Long fromUserId;
    private Long toUserId;
    private FriendshipStatus status;
}