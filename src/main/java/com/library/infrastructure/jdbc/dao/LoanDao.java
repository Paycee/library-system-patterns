package com.library.infrastructure.jdbc.dao;

import com.library.infrastructure.jdbc.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * "MAGACIONER" za tabelu loans.
 *
 * MySQL nema poseban UUID tip, pa id čuvamo kao CHAR(36):
 *   upis:    ps.setString(1, row.id().toString())
 *   čitanje: UUID.fromString(rs.getString("id"))
 *
 * Datumi:
 *   upis:    ps.setObject(i, localDate)        (za null: ps.setObject(i, null))
 *   čitanje: rs.getObject("due_date", LocalDate.class)
 */
public class  LoanDao {
    private final DataSource dataSource;

    public LoanDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ===================== Čitanje =====================

    public Optional<LoanRow> selectLatestByBookId(long bookId) {
        String sql = "SELECT * FROM loans WHERE book_id = ? ORDER BY (returned_at IS NULL) DESC, borrowed_at DESC LIMIT 1";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bookId);

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next())
                {
                    return Optional.of(new LoanRow(
                            UUID.fromString(rs.getString("id")),
                            rs.getLong("book_id"),
                            rs.getLong("member_id"),
                            rs.getObject("borrowed_at", LocalDate.class),
                            rs.getObject("due_date", LocalDate.class),
                            rs.getObject("returned_at", LocalDate.class)
                    ));
                }
                return Optional.empty();
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    public List<Long> selectBookIdsWithActiveLoan() {
        String sql = "SELECT book_id FROM loans WHERE returned_at IS NULL";
        List<Long> bookIds = new ArrayList<Long>();

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next())
                {
                    bookIds.add(rs.getLong("book_id"));
                }
                return bookIds;
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    public List<Long> selectBookIdsWithActiveLoanByMember(long memberId) {
        String sql = "SELECT book_id FROM loans WHERE returned_at IS NULL AND member_id = ?";
        List<Long> bookIds = new ArrayList<Long>();

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next())
                {
                    bookIds.add(rs.getLong("book_id"));
                }
                return bookIds;
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    public List<Long> selectBookIdsWithActiveLoanDueBefore(LocalDate date) {
        String sql = "SELECT book_id FROM loans WHERE returned_at IS NULL AND due_date < ?";
        List<Long> bookIds = new ArrayList<Long>();

        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, date);

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next())
                {
                    bookIds.add(rs.getLong("book_id"));
                }
                return bookIds;
            }
        }catch (SQLException e)
        {
            throw new DataAccessException("Opis greske", e);
        }
    }

    // ===================== Upis (u tuđoj transakciji) =====================

    public void upsert(Connection conn, LoanRow row) {
        String sql = """
                INSERT INTO loans (id, book_id, member_id, borrowed_at, due_date, returned_at)
                VALUES (?, ?, ?, ?, ?, ?) AS new
                ON DUPLICATE KEY UPDATE returned_at = new.returned_at
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {   // samo statement, ne i conn!
            ps.setString(1, row.id().toString());
            ps.setLong(2, row.bookId());
            ps.setLong(3, row.memberId());
            ps.setObject(4, row.borrowedAt());
            ps.setObject(5, row.dueDate());
            ps.setObject(6, row.returnedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Greška pri upisu knjige " + row.id(), e);
        }
    }
}
