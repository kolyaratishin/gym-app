package com.gymapp.dashboard.db;

import com.gymapp.dashboard.dto.ClientVisitStat;
import com.gymapp.dashboard.dto.VisitDayStat;
import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DashboardAnalyticsRepository extends BaseRepository {

    public DashboardAnalyticsRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public List<VisitDayStat> findVisitsByDayFrom(LocalDate from) {
        String sql = """
            SELECT substr(visit_time, 1, 10) AS visit_day,
                   COUNT(*) AS visits_count
            FROM visits
            WHERE substr(visit_time, 1, 10) >= ?
            GROUP BY substr(visit_time, 1, 10)
            ORDER BY visit_day
            """;

        return query(sql,
                ps -> ps.setString(1, from.toString()),
                rs -> new VisitDayStat(
                        LocalDate.parse(rs.getString("visit_day")),
                        rs.getLong("visits_count")
                )
        );
    }

    public List<ClientVisitStat> findTopClientsByVisits(int limit) {
        String sql = """
            SELECT c.client_number,
                   COALESCE(c.first_name, '') || ' ' || COALESCE(c.last_name, '') AS full_name,
                   COUNT(v.id) AS visits_count
            FROM visits v
            JOIN clients c ON c.id = v.client_id
            GROUP BY c.id
            ORDER BY visits_count DESC
            LIMIT ?
            """;

        return query(sql,
                ps -> ps.setInt(1, limit),
                this::mapClientVisitStat
        );
    }

    public List<ClientVisitStat> findActiveClientsWithoutVisits() {
        String sql = """
            SELECT c.client_number,
                   COALESCE(c.first_name, '') || ' ' || COALESCE(c.last_name, '') AS full_name,
                   0 AS visits_count
            FROM clients c
            JOIN memberships m ON m.client_id = c.id
            WHERE m.status = 'ACTIVE'
              AND NOT EXISTS (
                  SELECT 1
                  FROM visits v
                  WHERE v.membership_id = m.id
              )
            ORDER BY c.client_number
            """;

        return query(sql, null, this::mapClientVisitStat);
    }

    private ClientVisitStat mapClientVisitStat(ResultSet rs) throws SQLException {
        return new ClientVisitStat(
                getIntegerOrNull(rs, "client_number"),
                normalizeName(rs.getString("full_name")),
                rs.getLong("visits_count")
        );
    }

    private String normalizeName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}