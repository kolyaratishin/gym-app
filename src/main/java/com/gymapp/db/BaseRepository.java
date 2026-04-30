package com.gymapp.db;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseRepository {

    protected final ConnectionFactory connectionFactory;

    protected BaseRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    protected <T> List<T> query(String sql, StatementSetter setter, ResultSetMapper<T> mapper) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (setter != null) {
                setter.set(statement);
            }

            try (ResultSet rs = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Query failed", e);
        }
    }

    protected int update(String sql, StatementSetter setter) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (setter != null) {
                setter.set(statement);
            }

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Update failed", e);
        }
    }

    protected long insertAndReturnId(String sql, StatementSetter setter) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setter.set(statement);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new RuntimeException("No ID returned");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Insert failed", e);
        }
    }

    protected long queryForLong(String sql, StatementSetter setter) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (setter != null) {
                setter.set(statement);
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Query failed", e);
        }
    }

    // 🔥 JDBC helpers

    protected void setIntegerOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    protected Integer getIntegerOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    protected String toString(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    // 🔧 functional interfaces

    @FunctionalInterface
    public interface StatementSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
