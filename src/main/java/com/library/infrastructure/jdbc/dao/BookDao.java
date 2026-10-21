package com.library.infrastructure.jdbc.dao;

import com.library.infrastructure.jdbc.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BookDao {
    private final DataSource dataSource;

    public BookDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ===================== Čitanje =====================

    private BookRow mapRow(ResultSet rs) throws SQLException
    {
        return new BookRow(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("pages"),
                rs.getString("genre"),
                rs.getLong("shelf_id"));
    }


    public Optional<BookRow> selectById(long id) {
        String sql = "select * from books where id = ?";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next())
                {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    public List<BookRow> selectAll() {
        String sql = "select * from books";
        List<BookRow> books = new ArrayList<BookRow>();

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next())
                {
                    books.add(mapRow(rs));
                }
                return books;
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    public List<BookRow> selectByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(", ", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, title, author, pages, genre, shelf_id FROM books WHERE id IN (" + placeholders + ")";
        List<BookRow> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 1, ids.get(i));     // upitnici se broje od 1, lista od 0
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Greška pri čitanju knjiga " + ids, e);
        }
    }

    // ===================== Upis (u tuđoj transakciji) =====================

    public boolean exists(Connection conn, long id) {
        String sql = "select 1 from books where id = ?";

        try(PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next())
                {
                    return true;
                }
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
        return false;
    }

    public void insert(Connection conn, BookRow row) {
        String sql = "INSERT INTO books (id, title, author, pages, genre, shelf_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {   // samo statement, ne i conn!
            ps.setLong(1, row.id());
            ps.setString(2, row.title());
            ps.setString(3, row.author());
            ps.setInt(4, row.pages());
            ps.setString(5, row.genre());
            ps.setLong(6, row.shelfId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Greška pri upisu knjige " + row.id(), e);
        }

    }

    public void update(Connection conn, BookRow row) {

        String sql = "UPDATE books SET title = ?, author = ?, pages = ?, genre = ?, shelf_id = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {   // samo statement, ne i conn!
            ps.setString(1, row.title());
            ps.setString(2, row.author());
            ps.setInt(3, row.pages());
            ps.setString(4, row.genre());
            ps.setLong(5, row.shelfId());
            ps.setLong(6, row.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Greška pri upisu knjige " + row.id(), e);
        }
    }

    public List<BookRow> selectPage(int limit, int offset)
    {
        String sql = "SELECT id, title, author, pages, genre, shelf_id FROM books ORDER BY id LIMIT ? OFFSET ?";
        List<BookRow> result = new ArrayList<>();

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                    result.add(mapRow(rs));
                }
            }

            return result;
        }catch (SQLException e)
        {
            throw new DataAccessException("Greska pri citanju stranice knjiga", e);
        }
    }


}
