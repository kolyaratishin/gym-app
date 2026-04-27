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
                    first_name,
                    last_name,
                    phone,
                    birth_date,
                    notes,
                    registration_date,
                    active
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, client.getFirstName());
            statement.setString(2, client.getLastName());
            statement.setString(3, client.getPhone());
            statement.setString(4, client.getBirthDate() != null ? client.getBirthDate().toString() : null);
            statement.setString(5, client.getNotes());
            statement.setString(6, client.getRegistrationDate().toString());
            statement.setInt(7, client.isActive() ? 1 : 0);

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
                    Client client = mapClient(rs);
                    return Optional.of(client);
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
                first_name,
                last_name,
                phone,
                birth_date,
                notes,
                registration_date,
                active
            FROM clients
            ORDER BY id
            """;

        List<Client> clients = new ArrayList<>();

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                clients.add(mapClient(rs));
            }

            return clients;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all clients", e);
        }
    }

    @Override
    public List<Client> search(String query) {

        // 1. Спроба пошуку по id
        if (query != null && query.matches("\\d+")) {
            Optional<Client> byId = findById(Long.parseLong(query));
            if (byId.isPresent()) {
                return List.of(byId.get());
            }
        }

        // 2. Пошук по тексту
        String sql = """
            SELECT
                id,
                first_name,
                last_name,
                phone,
                birth_date,
                notes,
                registration_date,
                active
            FROM clients
            WHERE
                LOWER(first_name) LIKE ?
                OR LOWER(last_name) LIKE ?
                OR LOWER(phone) LIKE ?
                OR LOWER(first_name || ' ' || last_name) LIKE ?
            ORDER BY id
            """;

        List<Client> clients = new ArrayList<>();
        String searchPattern = "";
        if (query != null)
        {
            searchPattern = "%" + query.toLowerCase().trim() + "%";
        }

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapClient(rs));
                }
            }

            return clients;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search clients by query: " + query, e);
        }
    }

    @Override
    public void update(Client client) {
        String sql = """
            UPDATE clients
            SET
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

            statement.setString(1, client.getFirstName());
            statement.setString(2, client.getLastName());
            statement.setString(3, client.getPhone());
            statement.setString(4, client.getBirthDate() != null ? client.getBirthDate().toString() : null);
            statement.setString(5, client.getNotes());
            statement.setString(6, client.getRegistrationDate().toString());
            statement.setInt(7, client.isActive() ? 1 : 0);
            statement.setLong(8, client.getId());

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Client not found for update, id = " + client.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update client, id = " + client.getId(), e);
        }
    }

    @Override
    public void deactivate(Long clientId) {
        String sql = """
            UPDATE clients
            SET active = 0
            WHERE id = ?
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, clientId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Client not found for deactivate, id = " + clientId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate client, id = " + clientId, e);
        }
    }

    @Override
    public void reactivate(Long clientId) {
        String sql = """
            UPDATE clients
            SET active = 1
            WHERE id = ?
            """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, clientId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException("Client not found for reactivate, id = " + clientId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reactivate client, id = " + clientId, e);
        }
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getLong("id"));
        client.setFirstName(rs.getString("first_name"));
        client.setLastName(rs.getString("last_name"));
        client.setPhone(rs.getString("phone"));

        String birthDate = rs.getString("birth_date");
        if (birthDate != null) {
            client.setBirthDate(LocalDate.parse(birthDate));
        }

        client.setNotes(rs.getString("notes"));
        client.setRegistrationDate(LocalDate.parse(rs.getString("registration_date")));
        client.setActive(rs.getInt("active") == 1);

        return client;
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
}