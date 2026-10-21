package com.library.infrastructure.jdbc.dao;

import com.library.domain.member.Member;
import com.library.infrastructure.jdbc.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDao {

    private final DataSource dataSource;

    public MemberDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<MemberRow> selectById(long id)
    {
        String sql = "select * from members where id = ?";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next())
                {
                    return Optional.of(new MemberRow(
                            rs.getLong("id"),
                            rs.getString("name")
                    ));
                }
                return Optional.empty();
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    public void upsert(MemberRow row)
    {
        String sql = """
                INSERT INTO members (id, name) VALUES (?, ?) AS new
                                    ON DUPLICATE KEY UPDATE name = new.name
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, row.id());
            ps.setString(2, row.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Greška pri upisu clana " + row.id(), e);
        }
    }

    public List<MemberRow> selectAll() {
        String sql = "select * from members";
        List<MemberRow> shelfs = new ArrayList<MemberRow>();

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next())
                {
                    shelfs.add(new MemberRow(
                            rs.getLong("id"),
                            rs.getString("name")));
                }
                return shelfs;
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

}
