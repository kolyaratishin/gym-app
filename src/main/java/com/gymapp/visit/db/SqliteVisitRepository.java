package com.gymapp.visit.db;

import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.visit.dto.ClientVisitHistoryRow;

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

    @Override
    public List<ClientVisitHistoryRow> findHistoryByClientId(Long clientId) {
        String sql = """
        SELECT
            v.id AS visit_id,
            v.visit_time,
            m.id AS membership_id,
            mt.name AS membership_type_name,
            m.start_date AS membership_start_date,
            m.end_date AS membership_end_date,
            m.remaining_visits
        FROM visits v
        LEFT JOIN memberships m ON m.id = v.membership_id
        LEFT JOIN membership_types mt ON mt.id = m.membership_type_id
        WHERE v.client_id = ?
        ORDER BY v.visit_time DESC
        """;

        return query(sql,
                ps -> ps.setLong(1, clientId),
                this::mapClientVisitHistoryRow
        );
    }

    public boolean hasVisitToday(Long clientId) {
        String sql = """
        SELECT EXISTS(
            SELECT 1
            FROM visits
            WHERE client_id = ?
              AND DATE(visit_time) = DATE('now')
        )
    """;

        return queryForBoolean(
                sql,
                ps -> ps.setLong(1, clientId)
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

    private ClientVisitHistoryRow mapClientVisitHistoryRow(ResultSet rs) throws SQLException {
        return new ClientVisitHistoryRow(
                rs.getLong("visit_id"),
                LocalDateTime.parse(rs.getString("visit_time")),
                rs.getLong("membership_id"),
                rs.getString("membership_type_name"),
                parseDateOrNull(rs.getString("membership_start_date")),
                parseDateOrNull(rs.getString("membership_end_date")),
                getIntegerOrNull(rs, "remaining_visits")
        );
    }

    private LocalDate parseDateOrNull(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}