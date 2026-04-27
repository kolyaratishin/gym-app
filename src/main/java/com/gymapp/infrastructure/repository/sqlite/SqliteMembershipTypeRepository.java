package com.gymapp.infrastructure.repository.sqlite;

import com.gymapp.domain.membership.MembershipType;
import com.gymapp.domain.membership.VisitPolicy;
import com.gymapp.domain.repository.MembershipTypeRepository;
import com.gymapp.infrastructure.db.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteMembershipTypeRepository implements MembershipTypeRepository {

    private final ConnectionFactory connectionFactory;

    public SqliteMembershipTypeRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public MembershipType save(MembershipType membershipType) {
        String sql = """
                INSERT INTO membership_types (
                    name,
                    duration_days,
                    visit_limit,
                    price,
                    visit_policy,
                    active
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, membershipType.getName());
            setDurationDays(statement, 2, membershipType);
            setVisitLimit(statement, 3, membershipType);
            statement.setString(4, membershipType.getPrice().toPlainString());
            statement.setString(5, membershipType.getVisitPolicy().name());
            statement.setInt(6, membershipType.isActive() ? 1 : 0);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    membershipType.setId(keys.getLong(1));
                }
            }

            return membershipType;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save membership type", e);
        }
    }

    @Override
    public Optional<MembershipType> findById(Long id) {
        String sql = """
                SELECT
                    id,
                    name,
                    duration_days,
                    visit_limit,
                    price,
                    visit_policy,
                    active
                FROM membership_types
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMembershipType(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find membership type by id: " + id, e);
        }
    }

    @Override
    public List<MembershipType> findAll() {
        String sql = """
                SELECT
                    id,
                    name,
                    duration_days,
                    visit_limit,
                    price,
                    visit_policy,
                    active
                FROM membership_types
                ORDER BY id
                """;

        List<MembershipType> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapMembershipType(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all membership types", e);
        }
    }

    @Override
    public List<MembershipType> findActive() {
        String sql = """
                SELECT
                    id,
                    name,
                    duration_days,
                    visit_limit,
                    price,
                    visit_policy,
                    active
                FROM membership_types
                WHERE active = 1
                ORDER BY id
                """;

        List<MembershipType> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapMembershipType(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find active membership types", e);
        }
    }

    @Override
    public void update(MembershipType membershipType) {
        String sql = """
                UPDATE membership_types
                SET
                    name = ?,
                    duration_days = ?,
                    visit_limit = ?,
                    price = ?,
                    visit_policy = ?,
                    active = ?
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, membershipType.getName());
            setDurationDays(statement, 2, membershipType);
            setVisitLimit(statement, 3, membershipType);
            statement.setString(4, membershipType.getPrice().toPlainString());
            statement.setString(5, membershipType.getVisitPolicy().name());
            statement.setInt(6, membershipType.isActive() ? 1 : 0);
            statement.setLong(7, membershipType.getId());

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Membership type not found for update, id = " + membershipType.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update membership type, id = " + membershipType.getId(), e);
        }
    }

    @Override
    public void deactivate(Long id) {
        String sql = """
                UPDATE membership_types
                SET active = 0
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Membership type not found for deactivate, id = " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate membership type, id = " + id, e);
        }
    }

    @Override
    public void reactivate(Long id) {
        String sql = """
                UPDATE membership_types
                SET active = 1
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Membership type not found for reactivate, id = " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reactivate membership type, id = " + id, e);
        }
    }

    @Override
    public Optional<MembershipType> findByName(String name) {
        String sql = """
            SELECT
                id,
                name,
                duration_days,
                visit_limit,
                price,
                visit_policy,
                active
            FROM membership_types
            WHERE LOWER(name) = LOWER(?)
            LIMIT 1
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name.trim());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMembershipType(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find membership type by name: " + name, e);
        }
    }

    private void setDurationDays(PreparedStatement statement, int parameterIndex, MembershipType membershipType) throws SQLException {
        if (membershipType.getDurationDays() != null) {
            statement.setInt(parameterIndex, membershipType.getDurationDays());
        } else {
            statement.setNull(parameterIndex, Types.INTEGER);
        }
    }

    private void setVisitLimit(PreparedStatement statement, int parameterIndex, MembershipType membershipType) throws SQLException {
        VisitPolicy visitPolicy = membershipType.getVisitPolicy();

        if (visitPolicy == VisitPolicy.LIMITED_BY_VISITS) {
            if (membershipType.getVisitLimit() != null) {
                statement.setInt(parameterIndex, membershipType.getVisitLimit());
            } else {
                statement.setNull(parameterIndex, Types.INTEGER);
            }
            return;
        }

        statement.setNull(parameterIndex, Types.INTEGER);
    }

    private MembershipType mapMembershipType(ResultSet rs) throws SQLException {
        MembershipType membershipType = new MembershipType();

        membershipType.setId(rs.getLong("id"));
        membershipType.setName(rs.getString("name"));

        int durationDays = rs.getInt("duration_days");
        if (!rs.wasNull()) {
            membershipType.setDurationDays(durationDays);
        }

        int visitLimit = rs.getInt("visit_limit");
        if (!rs.wasNull()) {
            membershipType.setVisitLimit(visitLimit);
        }

        membershipType.setPrice(new BigDecimal(rs.getString("price")));
        membershipType.setVisitPolicy(VisitPolicy.valueOf(rs.getString("visit_policy")));
        membershipType.setActive(rs.getInt("active") == 1);

        return membershipType;
    }
}