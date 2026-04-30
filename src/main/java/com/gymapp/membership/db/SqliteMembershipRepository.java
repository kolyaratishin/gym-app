package com.gymapp.membership.db;

import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SqliteMembershipRepository extends BaseRepository implements MembershipRepository {

    public SqliteMembershipRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Membership save(Membership m) {
        String sql = """
            INSERT INTO memberships (
                client_id, membership_type_id, start_date,
                end_date, remaining_visits, status
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

        long id = insertAndReturnId(sql, ps -> {
            ps.setLong(1, m.getClientId());
            ps.setLong(2, m.getMembershipTypeId());
            ps.setString(3, m.getStartDate().toString());
            ps.setString(4, toString(m.getEndDate()));

            if (m.getRemainingVisits() != null) {
                ps.setInt(5, m.getRemainingVisits());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setString(6, m.getStatus().name());
        });

        m.setId(id);
        return m;
    }

    @Override
    public Optional<Membership> findById(Long id) {
        String sql = "SELECT * FROM memberships WHERE id = ?";

        return query(sql,
                ps -> ps.setLong(1, id),
                this::mapMembership
        ).stream().findFirst();
    }

    @Override
    public List<Membership> findAll() {
        return query(
                "SELECT * FROM memberships ORDER BY id",
                null,
                this::mapMembership
        );
    }

    @Override
    public List<Membership> findByClientId(Long clientId) {
        String sql = """
            SELECT * FROM memberships
            WHERE client_id = ?
            ORDER BY id DESC
            """;

        return query(sql,
                ps -> ps.setLong(1, clientId),
                this::mapMembership
        );
    }

    @Override
    public List<Membership> findByStatus(MembershipStatus status) {
        String sql = """
            SELECT * FROM memberships
            WHERE status = ?
            ORDER BY id DESC
            """;

        return query(sql,
                ps -> ps.setString(1, status.name()),
                this::mapMembership
        );
    }

    @Override
    public List<Membership> findExpiringUntil(LocalDate date) {
        String sql = """
            SELECT * FROM memberships
            WHERE end_date IS NOT NULL
              AND end_date <= ?
              AND status = ?
            ORDER BY end_date
            """;

        return query(sql,
                ps -> {
                    ps.setString(1, date.toString());
                    ps.setString(2, MembershipStatus.ACTIVE.name());
                },
                this::mapMembership
        );
    }

    @Override
    public Optional<Membership> findActiveByClientId(Long clientId) {
        String sql = """
            SELECT * FROM memberships
            WHERE client_id = ?
              AND status = ?
            ORDER BY id DESC
            LIMIT 1
            """;

        return query(sql,
                ps -> {
                    ps.setLong(1, clientId);
                    ps.setString(2, MembershipStatus.ACTIVE.name());
                },
                this::mapMembership
        ).stream().findFirst();
    }

    @Override
    public void update(Membership m) {
        String sql = """
            UPDATE memberships
            SET client_id = ?, membership_type_id = ?, start_date = ?,
                end_date = ?, remaining_visits = ?, status = ?
            WHERE id = ?
            """;

        int updated = update(sql, ps -> {
            ps.setLong(1, m.getClientId());
            ps.setLong(2, m.getMembershipTypeId());
            ps.setString(3, m.getStartDate().toString());
            ps.setString(4, toString(m.getEndDate()));

            if (m.getRemainingVisits() != null) {
                ps.setInt(5, m.getRemainingVisits());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setString(6, m.getStatus().name());
            ps.setLong(7, m.getId());
        });

        if (updated == 0) {
            throw new RuntimeException("Membership not found: " + m.getId());
        }
    }

    @Override
    public void expireById(Long id) {
        int updated = update(
                "UPDATE memberships SET status = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, MembershipStatus.EXPIRED.name());
                    ps.setLong(2, id);
                }
        );

        if (updated == 0) {
            throw new RuntimeException("Membership not found: " + id);
        }
    }

    @Override
    public void deactivateActiveByClientId(Long clientId) {
        update(
                "UPDATE memberships SET status = ? WHERE client_id = ? AND status = ?",
                ps -> {
                    ps.setString(1, MembershipStatus.CANCELLED.name());
                    ps.setLong(2, clientId);
                    ps.setString(3, MembershipStatus.ACTIVE.name());
                }
        );
    }

    @Override
    public void expireOutdatedMemberships(LocalDate today) {
        update(
                """
                UPDATE memberships
                SET status = ?
                WHERE status = ?
                  AND end_date IS NOT NULL
                  AND end_date < ?
                """,
                ps -> {
                    ps.setString(1, MembershipStatus.EXPIRED.name());
                    ps.setString(2, MembershipStatus.ACTIVE.name());
                    ps.setString(3, today.toString());
                }
        );
    }

    @Override
    public long countClientsWithActiveMembership() {
        return queryForLong(
                "SELECT COUNT(DISTINCT client_id) FROM memberships WHERE status = ?",
                ps -> ps.setString(1, MembershipStatus.ACTIVE.name())
        );
    }

    private Membership mapMembership(ResultSet rs) throws SQLException {
        Membership m = new Membership();

        m.setId(rs.getLong("id"));
        m.setClientId(rs.getLong("client_id"));
        m.setMembershipTypeId(rs.getLong("membership_type_id"));
        m.setStartDate(LocalDate.parse(rs.getString("start_date")));

        String endDate = rs.getString("end_date");
        if (endDate != null) {
            m.setEndDate(LocalDate.parse(endDate));
        }

        int remainingVisits = rs.getInt("remaining_visits");
        if (!rs.wasNull()) {
            m.setRemainingVisits(remainingVisits);
        }

        m.setStatus(MembershipStatus.valueOf(rs.getString("status")));

        return m;
    }
}
