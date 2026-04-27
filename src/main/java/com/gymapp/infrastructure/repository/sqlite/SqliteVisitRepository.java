package com.gymapp.infrastructure.repository.sqlite;

import com.gymapp.domain.repository.VisitRepository;
import com.gymapp.domain.visit.Visit;
import com.gymapp.infrastructure.db.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteVisitRepository implements VisitRepository {

    private final ConnectionFactory connectionFactory;

    public SqliteVisitRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
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

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, visit.getClientId());
            statement.setLong(2, visit.getMembershipId());
            statement.setString(3, visit.getVisitTime().toString());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    visit.setId(keys.getLong(1));
                }
            }

            return visit;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save visit", e);
        }
    }

    @Override
    public Optional<Visit> findById(Long id) {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_id,
                    visit_time
                FROM visits
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapVisit(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find visit by id: " + id, e);
        }
    }

    @Override
    public List<Visit> findAll() {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_id,
                    visit_time
                FROM visits
                ORDER BY visit_time DESC
                """;

        List<Visit> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapVisit(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all visits", e);
        }
    }

    @Override
    public long countByDate(LocalDate date) {
        String sql = """
            SELECT COUNT(*)
            FROM visits
            WHERE DATE(visit_time) = ?
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, date.toString());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count visits by date: " + date, e);
        }
    }

    @Override
    public List<Visit> findByClientId(Long clientId) {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_id,
                    visit_time
                FROM visits
                WHERE client_id = ?
                ORDER BY visit_time DESC
                """;

        List<Visit> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, clientId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapVisit(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find visits by client id: " + clientId, e);
        }
    }

    @Override
    public List<Visit> findByMembershipId(Long membershipId) {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_id,
                    visit_time
                FROM visits
                WHERE membership_id = ?
                ORDER BY visit_time DESC
                """;

        List<Visit> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, membershipId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapVisit(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find visits by membership id: " + membershipId, e);
        }
    }

    @Override
    public List<Visit> findByDate(LocalDate date) {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_id,
                    visit_time
                FROM visits
                WHERE DATE(visit_time) = ?
                ORDER BY visit_time DESC
                """;

        List<Visit> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, date.toString());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapVisit(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find visits by date: " + date, e);
        }
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