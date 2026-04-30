package com.gymapp.visit.db;

import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SqliteVisitRepository extends BaseRepository implements VisitRepository {

    public SqliteVisitRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Visit save(Visit visit) {
        String sql = """
            INSERT INTO visits (
                client_id,
                membership_id,
                visit_time
            ) VALUES (?, ?, ?)
            """;

        long id = insertAndReturnId(sql, ps -> {
            ps.setLong(1, visit.getClientId());
            ps.setLong(2, visit.getMembershipId());
            ps.setString(3, visit.getVisitTime().toString());
        });

        visit.setId(id);
        return visit;
    }

    @Override
    public Optional<Visit> findById(Long id) {
        String sql = "SELECT * FROM visits WHERE id = ?";

        return query(sql,
                ps -> ps.setLong(1, id),
                this::mapVisit
        ).stream().findFirst();
    }

    @Override
    public List<Visit> findAll() {
        return query(
                "SELECT * FROM visits ORDER BY visit_time DESC",
                null,
                this::mapVisit
        );
    }

    @Override
    public long countByDate(LocalDate date) {
        String sql = """
            SELECT COUNT(*)
            FROM visits
            WHERE DATE(visit_time) = ?
            """;

        return queryForLong(sql, ps -> ps.setString(1, date.toString()));
    }

    @Override
    public List<Visit> findByClientId(Long clientId) {
        String sql = """
            SELECT * FROM visits
            WHERE client_id = ?
            ORDER BY visit_time DESC
            """;

        return query(sql,
                ps -> ps.setLong(1, clientId),
                this::mapVisit
        );
    }

    @Override
    public List<Visit> findByMembershipId(Long membershipId) {
        String sql = """
            SELECT * FROM visits
            WHERE membership_id = ?
            ORDER BY visit_time DESC
            """;

        return query(sql,
                ps -> ps.setLong(1, membershipId),
                this::mapVisit
        );
    }

    @Override
    public List<Visit> findByDate(LocalDate date) {
        String sql = """
            SELECT * FROM visits
            WHERE DATE(visit_time) = ?
            ORDER BY visit_time DESC
            """;

        return query(sql,
                ps -> ps.setString(1, date.toString()),
                this::mapVisit
        );
    }

    private Visit mapVisit(ResultSet rs) throws SQLException {
        Visit visit = new Visit();

        visit.setId(rs.getLong("id"));
        visit.setClientId(rs.getLong("client_id"));
        visit.setMembershipId(rs.getLong("membership_id"));
        visit.setVisitTime(LocalDateTime.parse(rs.getString("visit_time")));

        return visit;
    }
}