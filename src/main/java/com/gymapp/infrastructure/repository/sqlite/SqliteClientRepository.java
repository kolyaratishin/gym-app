package com.gymapp.infrastructure.repository.sqlite;

import com.gymapp.domain.client.Client;
import com.gymapp.domain.repository.ClientRepository;
import com.gymapp.infrastructure.db.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteClientRepository implements ClientRepository {

    private final ConnectionFactory connectionFactory;

    public SqliteClientRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Client save(Client client) {
        String sql = """
                INSERT INTO clients (
                    client_number,
                    first_name,
                    last_name,
                    phone,
                    birth_date,
                    notes,
                    registration_date,
                    active
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setIntegerOrNull(statement, 1, client.getClientNumber());
            statement.setString(2, client.getFirstName());
            statement.setString(3, client.getLastName());
            statement.setString(4, client.getPhone());
            statement.setString(5, client.getBirthDate() != null ? client.getBirthDate().toString() : null);
            statement.setString(6, client.getNotes());
            statement.setString(7, client.getRegistrationDate() != null ? client.getRegistrationDate().toString() : null);
            statement.setInt(8, client.isActive() ? 1 : 0);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getLong(1));
                }
            }

            return client;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save client", e);
        }
    }

    @Override
    public Optional<Client> findById(Long id) {
        String sql = """
                SELECT
                    id,
                    client_number,
                    first_name,
                    last_name,
                    phone,
                    birth_date,
                    notes,
                    registration_date,
                    active
                FROM clients
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapClient(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find client by id: " + id, e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = """
                SELECT
                    id,
                    client_number,
                    first_name,
                    last_name,
                    phone,
                    birth_date,
                    notes,
                    registration_date,
                    active
                FROM clients
                ORDER BY
                    CASE WHEN client_number IS NULL THEN 1 ELSE 0 END,
                    client_number,
                    id
                """;

        List<Client> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapClient(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all clients", e);
        }
    }

    @Override
    public List<Client> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }

        String trimmedQuery = query.trim();

        if (isLong(trimmedQuery)) {
            Optional<Client> clientById = findById(Long.parseLong(trimmedQuery));
            if (clientById.isPresent()) {
                return List.of(clientById.get());
            }

            List<Client> byClientNumber = findByClientNumber(Integer.parseInt(trimmedQuery));
            if (!byClientNumber.isEmpty()) {
                return byClientNumber;
            }
        }

        String sql = """
                SELECT
                    id,
                    client_number,
                    first_name,
                    last_name,
                    phone,
                    birth_date,
                    notes,
                    registration_date,
                    active
                FROM clients
                WHERE LOWER(COALESCE(first_name, '')) LIKE LOWER(?)
                   OR LOWER(COALESCE(last_name, '')) LIKE LOWER(?)
                   OR LOWER(COALESCE(phone, '')) LIKE LOWER(?)
                ORDER BY
                    CASE WHEN client_number IS NULL THEN 1 ELSE 0 END,
                    client_number,
                    id
                """;

        List<Client> result = new ArrayList<>();
        String pattern = "%" + trimmedQuery + "%";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapClient(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search clients by query: " + query, e);
        }
    }

    @Override
    public void update(Client client) {
        String sql = """
                UPDATE clients
                SET
                    client_number = ?,
                    first_name = ?,
                    last_name = ?,
                    phone = ?,
                    birth_date = ?,
                    notes = ?,
                    registration_date = ?,
                    active = ?
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setIntegerOrNull(statement, 1, client.getClientNumber());
            statement.setString(2, client.getFirstName());
            statement.setString(3, client.getLastName());
            statement.setString(4, client.getPhone());
            statement.setString(5, client.getBirthDate() != null ? client.getBirthDate().toString() : null);
            statement.setString(6, client.getNotes());
            statement.setString(7, client.getRegistrationDate() != null ? client.getRegistrationDate().toString() : null);
            statement.setInt(8, client.isActive() ? 1 : 0);
            statement.setLong(9, client.getId());

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Client not found for update, id = " + client.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update client, id = " + client.getId(), e);
        }
    }

    @Override
    public void deactivate(Long id) {
        String sql = """
                UPDATE clients
                SET active = 0
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Client not found for deactivate, id = " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate client, id = " + id, e);
        }
    }

    @Override
    public void reactivate(Long id) {
        String sql = """
                UPDATE clients
                SET active = 1
                WHERE id = ?
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Client not found for reactivate, id = " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reactivate client, id = " + id, e);
        }
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM clients";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count all clients", e);
        }
    }

    @Override
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM clients WHERE active = 1";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count active clients", e);
        }
    }

    private List<Client> findByClientNumber(Integer clientNumber) {
        String sql = """
                SELECT
                    id,
                    client_number,
                    first_name,
                    last_name,
                    phone,
                    birth_date,
                    notes,
                    registration_date,
                    active
                FROM clients
                WHERE client_number = ?
                ORDER BY id
                """;

        List<Client> result = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, clientNumber);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapClient(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find client by client number: " + clientNumber, e);
        }
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        Client client = new Client();

        client.setId(rs.getLong("id"));
        client.setClientNumber(getIntegerOrNull(rs, "client_number"));
        client.setFirstName(rs.getString("first_name"));
        client.setLastName(rs.getString("last_name"));
        client.setPhone(rs.getString("phone"));

        String birthDate = rs.getString("birth_date");
        client.setBirthDate(birthDate != null ? LocalDate.parse(birthDate) : null);

        client.setNotes(rs.getString("notes"));

        String registrationDate = rs.getString("registration_date");
        client.setRegistrationDate(registrationDate != null ? LocalDate.parse(registrationDate) : null);

        client.setActive(rs.getInt("active") == 1);

        return client;
    }

    private void setIntegerOrNull(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private Integer getIntegerOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean existsByClientNumber(Integer clientNumber) {
        String sql = """
            SELECT 1
            FROM clients
            WHERE client_number = ?
            LIMIT 1
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, clientNumber);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check client number: " + clientNumber, e);
        }
    }
}