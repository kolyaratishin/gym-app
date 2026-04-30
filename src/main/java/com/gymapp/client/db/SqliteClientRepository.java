package com.gymapp.client.db;

import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SqliteClientRepository extends BaseRepository implements ClientRepository {
    public SqliteClientRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Client save(Client client) {
        String sql = """
                INSERT INTO clients (
                    client_number, first_name, last_name, phone,
                    birth_date, notes, registration_date, active
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        long id = insertAndReturnId(sql, ps -> {
            setClientFields(client, ps);
        });

        client.setId(id);
        return client;
    }

    public Optional<Client> findById(Long id) {
        String sql = "SELECT * FROM clients WHERE id = ?";

        List<Client> result = query(sql,
                ps -> ps.setLong(1, id),
                this::mapClient
        );

        return result.stream().findFirst();
    }

    public List<Client> findAll() {
        String sql = """
                SELECT * FROM clients
                ORDER BY
                    CASE WHEN client_number IS NULL THEN 1 ELSE 0 END,
                    client_number,
                    id
                """;

        return query(sql, null, this::mapClient);
    }

    @Override
    public List<Client> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }

        String trimmed = query.trim();

        if (isLong(trimmed)) {
            Optional<Client> byId = findById(Long.parseLong(trimmed));
            if (byId.isPresent()) {
                return List.of(byId.get());
            }

            List<Client> byNumber = findByClientNumber(Integer.parseInt(trimmed));
            if (!byNumber.isEmpty()) {
                return byNumber;
            }
        }

        String sql = """
                SELECT * FROM clients
                WHERE LOWER(COALESCE(first_name, '')) LIKE LOWER(?)
                   OR LOWER(COALESCE(last_name, '')) LIKE LOWER(?)
                   OR LOWER(COALESCE(phone, '')) LIKE LOWER(?)
                ORDER BY
                    CASE WHEN client_number IS NULL THEN 1 ELSE 0 END,
                    client_number,
                    id
                """;

        String pattern = "%" + trimmed + "%";

        return query(sql,
                ps -> {
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                    ps.setString(3, pattern);
                },
                this::mapClient
        );
    }

    @Override
    public void update(Client client) {
        String sql = """
                UPDATE clients
                SET client_number = ?, first_name = ?, last_name = ?, phone = ?,
                    birth_date = ?, notes = ?, registration_date = ?, active = ?
                WHERE id = ?
                """;

        int updated = update(sql, ps -> {
            setClientFields(client, ps);
            ps.setLong(9, client.getId());
        });

        if (updated == 0) {
            throw new RuntimeException("Client not found: " + client.getId());
        }
    }

    private void setClientFields(Client client, PreparedStatement ps) throws SQLException {
        setIntegerOrNull(ps, 1, client.getClientNumber());
        ps.setString(2, client.getFirstName());
        ps.setString(3, client.getLastName());
        ps.setString(4, client.getPhone());
        ps.setString(5, toString(client.getBirthDate()));
        ps.setString(6, client.getNotes());
        ps.setString(7, toString(client.getRegistrationDate()));
        ps.setInt(8, client.isActive() ? 1 : 0);
    }

    @Override
    public void deactivate(Long id) {
        String sql = "UPDATE clients SET active = 0 WHERE id = ?";

        int updated = update(sql, ps -> ps.setLong(1, id));

        if (updated == 0) {
            throw new RuntimeException("Client not found: " + id);
        }
    }

    @Override
    public void reactivate(Long id) {
        String sql = "UPDATE clients SET active = 1 WHERE id = ?";

        int updated = update(sql, ps -> ps.setLong(1, id));

        if (updated == 0) {
            throw new RuntimeException("Client not found: " + id);
        }
    }

    @Override
    public long countAll() {
        return queryForLong("SELECT COUNT(*) FROM clients", null);
    }

    @Override
    public long countActive() {
        return queryForLong("SELECT COUNT(*) FROM clients WHERE active = 1", null);
    }

    private List<Client> findByClientNumber(Integer clientNumber) {
        String sql = """
        SELECT * FROM clients
        WHERE client_number = ?
        ORDER BY id
        """;

        return query(sql,
                ps -> ps.setInt(1, clientNumber),
                this::mapClient
        );
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

    @Override
    public boolean existsByClientNumber(Integer clientNumber) {
        String sql = "SELECT 1 FROM clients WHERE client_number = ? LIMIT 1";

        List<Integer> result = query(sql,
                ps -> ps.setInt(1, clientNumber),
                rs -> rs.getInt(1)
        );

        return !result.isEmpty();
    }

    private boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}