package com.gymapp.membership.db;

import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteMembershipRepository implements MembershipRepository {

    private final ConnectionFactory connectionFactory;

    public SqliteMembershipRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Membership save(Membership membership) {
        String sql = """
                INSERT INTO memberships (
                    client_id,
                    membership_type_id,
                    start_date,
                    end_date,
                    remaining_visits,
                    status
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, membership.getClientId());
            statement.setLong(2, membership.getMembershipTypeId());
            statement.setString(3, membership.getStartDate().toString());
            statement.setString(4, membership.getEndDate() != null ? membership.getEndDate().toString() : null);

            if (membership.getRemainingVisits() != null) {
                statement.setInt(5, membership.getRemainingVisits());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            statement.setString(6, membership.getStatus().name());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    membership.setId(keys.getLong(1));
                }
            }

            return membership;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save membership", e);
        }
    }

    @Override
    public Optional<Membership> findById(Long id) {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_type_id,
                    start_date,
                    end_date,
                    remaining_visits,
                    status
                FROM memberships
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMembership(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find membership by id: " + id, e);
        }
    }

    @Override
    public List<Membership> findAll() {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_type_id,
                    start_date,
                    end_date,
                    remaining_visits,
                    status
                FROM memberships
                ORDER BY id
                """;

        List<Membership> memberships = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                memberships.add(mapMembership(rs));
            }

            return memberships;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all memberships", e);
        }
    }

    @Override
    public List<Membership> findByClientId(Long clientId) {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_type_id,
                    start_date,
                    end_date,
                    remaining_visits,
                    status
                FROM memberships
                WHERE client_id = ?
                ORDER BY id DESC
                """;

        List<Membership> memberships = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, clientId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    memberships.add(mapMembership(rs));
                }
            }

            return memberships;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find memberships by client id: " + clientId, e);
        }
    }

    @Override
    public List<Membership> findByStatus(MembershipStatus status) {
        String sql = """
            SELECT
                id,
                client_id,
                membership_type_id,
                start_date,
                end_date,
                remaining_visits,
                status
            FROM memberships
            WHERE status = ?
            ORDER BY id DESC
            """;

        List<Membership> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapMembership(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find memberships by status: " + status, e);
        }
    }

    @Override
    public List<Membership> findExpiringUntil(LocalDate date) {
        String sql = """
            SELECT
                id,
                client_id,
                membership_type_id,
                start_date,
                end_date,
                remaining_visits,
                status
            FROM memberships
            WHERE
                end_date IS NOT NULL
                AND end_date <= ?
                AND status = ?
            ORDER BY end_date
            """;

        List<Membership> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, date.toString());
            statement.setString(2, MembershipStatus.ACTIVE.name());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapMembership(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find expiring memberships until: " + date, e);
        }
    }

    @Override
    public Optional<Membership> findActiveByClientId(Long clientId) {
        String sql = """
                SELECT
                    id,
                    client_id,
                    membership_type_id,
                    start_date,
                    end_date,
                    remaining_visits,
                    status
                FROM memberships
                WHERE client_id = ?
                  AND status = ?
                ORDER BY id DESC
                LIMIT 1
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, clientId);
            statement.setString(2, MembershipStatus.ACTIVE.name());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMembership(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find active membership by client id: " + clientId, e);
        }
    }

    @Override
    public void update(Membership membership) {
        String sql = """
                UPDATE memberships
                SET
                    client_id = ?,
                    membership_type_id = ?,
                    start_date = ?,
                    end_date = ?,
                    remaining_visits = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, membership.getClientId());
            statement.setLong(2, membership.getMembershipTypeId());
            statement.setString(3, membership.getStartDate().toString());
            statement.setString(4, membership.getEndDate() != null ? membership.getEndDate().toString() : null);

            if (membership.getRemainingVisits() != null) {
                statement.setInt(5, membership.getRemainingVisits());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            statement.setString(6, membership.getStatus().name());
            statement.setLong(7, membership.getId());

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Membership not found for update, id = " + membership.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update membership, id = " + membership.getId(), e);
        }
    }

    @Override
    public void expireById(Long membershipId) {
        String sql = """
                UPDATE memberships
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, MembershipStatus.EXPIRED.name());
            statement.setLong(2, membershipId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Membership not found for expire, id = " + membershipId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to expire membership, id = " + membershipId, e);
        }
    }

    @Override
    public void deactivateActiveByClientId(Long clientId) {
        String sql = """
            UPDATE memberships
            SET status = ?
            WHERE client_id = ?
              AND status = ?
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, MembershipStatus.CANCELLED.name());
            statement.setLong(2, clientId);
            statement.setString(3, MembershipStatus.ACTIVE.name());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate active membership for client id: " + clientId, e);
        }
    }

    @Override
    public void expireOutdatedMemberships(LocalDate today) {
        String sql = """
            UPDATE memberships
            SET status = ?
            WHERE status = ?
              AND end_date IS NOT NULL
              AND end_date < ?
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, MembershipStatus.EXPIRED.name());
            statement.setString(2, MembershipStatus.ACTIVE.name());
            statement.setString(3, today.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to expire outdated memberships", e);
        }
    }

    @Override
    public long countClientsWithActiveMembership() {
        String sql = """
            SELECT COUNT(DISTINCT client_id)
            FROM memberships
            WHERE status = ?
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, MembershipStatus.ACTIVE.name());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count clients with active membership", e);
        }
    }

    private Membership mapMembership(ResultSet rs) throws SQLException {
        Membership membership = new Membership();

        membership.setId(rs.getLong("id"));
        membership.setClientId(rs.getLong("client_id"));
        membership.setMembershipTypeId(rs.getLong("membership_type_id"));
        membership.setStartDate(LocalDate.parse(rs.getString("start_date")));

        String endDate = rs.getString("end_date");
        if (endDate != null) {
            membership.setEndDate(LocalDate.parse(endDate));
        }

        int remainingVisits = rs.getInt("remaining_visits");
        if (!rs.wasNull()) {
            membership.setRemainingVisits(remainingVisits);
        }

        membership.setStatus(MembershipStatus.valueOf(rs.getString("status")));

        return membership;
    }
}