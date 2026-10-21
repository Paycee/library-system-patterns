package com.library.infrastructure.jdbc.dao;

import com.library.infrastructure.jdbc.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** "MAGACIONER" za tabelu shelves. Polica je jedan red, pa ovde nema potrebe za zajedničkom transakcijom. */
public class ShelfDao {
    private final DataSource dataSource;

    public ShelfDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private ShelfRow mapRow(ResultSet rs) throws SQLException
    {
        return new ShelfRow(
                rs.getLong("id"),
                rs.getString("label"));
    }

    public Optional<ShelfRow> selectById(long id) {
        String sql = "select * from shelfes where id = ?";

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

    public List<ShelfRow> selectAll() {
        String sql = "select * from shelfes";
        List<ShelfRow> shelfs = new ArrayList<ShelfRow>();

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next())
                {
                    shelfs.add(mapRow(rs));
                }
                return shelfs;
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    public void upsert(ShelfRow row) {
        String sql = """
                INSERT INTO shelves (id, label) VALUES (?, ?) AS new
                                    ON DUPLICATE KEY UPDATE label = new.label
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, row.id());
            ps.setString(2, row.label());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Greška pri upisu police " + row.id(), e);
        }
    }
}
