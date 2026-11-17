package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM directors WHERE id IN (%s) ORDER BY id";
    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Director> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Director> getById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Director> getByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return findMany(String.format(FIND_BY_IDS_QUERY, placeholders), ids.toArray());
    }

    @Override
    public Director create(Director director) {
        long id = insert(INSERT_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public boolean update(Director director) {
        return update(UPDATE_QUERY, director.getName(), director.getId()) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return update(DELETE_QUERY, id) > 0;
    }
}
