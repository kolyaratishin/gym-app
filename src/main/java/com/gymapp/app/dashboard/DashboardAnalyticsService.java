package com.gymapp.app.dashboard;

import com.gymapp.infrastructure.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardAnalyticsService {

    private final ConnectionFactory connectionFactory;

    public DashboardAnalyticsService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public List<VisitDayStat> getVisitsByDayLast30Days() {
        String sql = """
                SELECT substr(visit_time, 1, 10) AS visit_day,
                       COUNT(*) AS visits_count
                FROM visits
                WHERE substr(visit_time, 1, 10) >= ?
                GROUP BY substr(visit_time, 1, 10)
                ORDER BY visit_day
                """;

        LocalDate from = LocalDate.now().minusDays(29);
        List<VisitDayStat> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, from.toString());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new VisitDayStat(
                            LocalDate.parse(rs.getString("visit_day")),
                            rs.getLong("visits_count")
                    ));
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load visits by day", e);
        }
    }

    public List<ClientVisitStat> getTopClientsByVisits(int limit) {
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

        List<ClientVisitStat> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int number = rs.getInt("client_number");
                    Integer clientNumber = rs.wasNull() ? null : number;

                    result.add(new ClientVisitStat(
                            clientNumber,
                            normalizeName(rs.getString("full_name")),
                            rs.getLong("visits_count")
                    ));
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load top clients", e);
        }
    }

    public List<ClientVisitStat> getActiveClientsWithoutVisits() {
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

        List<ClientVisitStat> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                int number = rs.getInt("client_number");
                Integer clientNumber = rs.wasNull() ? null : number;

                result.add(new ClientVisitStat(
                        clientNumber,
                        normalizeName(rs.getString("full_name")),
                        rs.getLong("visits_count")
                ));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load active clients without visits", e);
        }
    }

    private String normalizeName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}