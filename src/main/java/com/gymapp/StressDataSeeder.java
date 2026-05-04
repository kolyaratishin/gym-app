package com.gymapp;

import com.gymapp.db.ConnectionFactory;
import com.gymapp.db.SqliteConnectionFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

public class StressDataSeeder {

    private static final int CLIENTS_COUNT = 1_000;
    private static final int VISITS_COUNT = 1_000_000;

    private static final Random RANDOM = new Random();

    private static final String[] FIRST_NAMES = {
            "Іван", "Олена", "Андрій", "Марія", "Олег",
            "Наталія", "Василь", "Юлія", "Роман", "Ірина"
    };

    private static final String[] LAST_NAMES = {
            "Петренко", "Коваль", "Мельник", "Шевченко", "Бондар",
            "Гриценко", "Кравець", "Ткаченко", "Савчук", "Лисенко"
    };

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();

        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            long membershipTypeId = createMembershipType(connection);
            long[] clientIds = new long[CLIENTS_COUNT];
            long[] membershipIds = new long[CLIENTS_COUNT];

            insertClientsAndMemberships(connection, membershipTypeId, clientIds, membershipIds);
            insertVisits(connection, clientIds, membershipIds);
            createIndexes(connection);

            connection.commit();

            System.out.println("Done");
            System.out.println("Clients inserted: " + CLIENTS_COUNT);
            System.out.println("Visits inserted: " + VISITS_COUNT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed stress data", e);
        }
    }

    private static long createMembershipType(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO membership_types (
                    name,
                    duration_days,
                    visit_limit,
                    price,
                    visit_policy,
                    active
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "Stress Test Абонемент");
            statement.setInt(2, 180);
            statement.setInt(3, 9999);
            statement.setString(4, BigDecimal.ZERO.toPlainString());
            statement.setString(5, "LIMITED_BY_VISITS");
            statement.setInt(6, 1);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("Failed to create membership type");
    }

    private static void insertClientsAndMemberships(
            Connection connection,
            long membershipTypeId,
            long[] clientIds,
            long[] membershipIds
    ) throws SQLException {
        String clientSql = """
                INSERT INTO clients (
                    client_number,
                    first_name,
                    last_name,
                    phone,
                    birth_date,
                    notes,
                    registration_date,
                    active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String membershipSql = """
                INSERT INTO memberships (
                    client_id,
                    membership_type_id,
                    start_date,
                    end_date,
                    remaining_visits,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement clientStatement = connection.prepareStatement(clientSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement membershipStatement = connection.prepareStatement(membershipSql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < CLIENTS_COUNT; i++) {
                int clientNumber = 100_000 + i;

                clientStatement.setInt(1, clientNumber);
                clientStatement.setString(2, randomFirstName());
                clientStatement.setString(3, randomLastName() + " " + clientNumber);
                clientStatement.setString(4, "+38067" + randomDigits(7));
                clientStatement.setString(5, randomBirthDate().toString());
                clientStatement.setString(6, "Stress test client");
                clientStatement.setString(7, randomPastDate(365).toString());
                clientStatement.setInt(8, 0);

                clientStatement.executeUpdate();

                long clientId;
                try (ResultSet keys = clientStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to get generated client id");
                    }
                    clientId = keys.getLong(1);
                }

                clientIds[i] = clientId;

                LocalDate startDate = randomPastDate(180);
                LocalDate endDate = startDate.plusDays(180);

                membershipStatement.setLong(1, clientId);
                membershipStatement.setLong(2, membershipTypeId);
                membershipStatement.setString(3, startDate.toString());
                membershipStatement.setString(4, endDate.toString());
                membershipStatement.setInt(5, RANDOM.nextInt(100) + 1);
                membershipStatement.setString(6, "ACTIVE");

                membershipStatement.executeUpdate();

                long membershipId;
                try (ResultSet keys = membershipStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to get generated membership id");
                    }
                    membershipId = keys.getLong(1);
                }

                membershipIds[i] = membershipId;

                if ((i + 1) % 1000 == 0) {
                    System.out.println("Inserted clients: " + (i + 1));
                }
            }
        }
    }

    private static void insertVisits(
            Connection connection,
            long[] clientIds,
            long[] membershipIds
    ) throws SQLException {
        String sql = """
                INSERT INTO visits (
                    client_id,
                    membership_id,
                    visit_time
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < VISITS_COUNT; i++) {
                int index = RANDOM.nextInt(CLIENTS_COUNT);

                statement.setLong(1, clientIds[index]);
                statement.setLong(2, membershipIds[index]);
                statement.setString(3, randomPastDateTime(365).toString());

                statement.addBatch();

                if ((i + 1) % 5000 == 0) {
                    statement.executeBatch();
                    System.out.println("Inserted visits: " + (i + 1));
                }
            }

            statement.executeBatch();
        }
    }

    private static void createIndexes(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE INDEX IF NOT EXISTS idx_clients_client_number ON clients(client_number)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_memberships_client_status ON memberships(client_id, status)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_visits_client_id ON visits(client_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_visits_visit_time ON visits(visit_time)");
        }
    }

    private static String randomFirstName() {
        return FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
    }

    private static String randomLastName() {
        return LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
    }

    private static String randomDigits(int count) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < count; i++) {
            result.append(RANDOM.nextInt(10));
        }

        return result.toString();
    }

    private static LocalDate randomBirthDate() {
        return LocalDate.now().minusYears(18 + RANDOM.nextInt(45)).minusDays(RANDOM.nextInt(365));
    }

    private static LocalDate randomPastDate(int maxDaysBack) {
        return LocalDate.now().minusDays(RANDOM.nextInt(maxDaysBack + 1));
    }

    private static LocalDateTime randomPastDateTime(int maxDaysBack) {
        return LocalDateTime.now()
                .minusDays(RANDOM.nextInt(maxDaysBack + 1))
                .minusHours(RANDOM.nextInt(12))
                .minusMinutes(RANDOM.nextInt(60))
                .withNano(0);
    }
}