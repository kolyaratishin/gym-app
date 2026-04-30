package com.gymapp.membership.db;

import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

public class SqliteMembershipTypeRepository extends BaseRepository implements MembershipTypeRepository {

    public SqliteMembershipTypeRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
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

        long id = insertAndReturnId(sql, ps -> setMembershipTypeFields(membershipType, ps));

        membershipType.setId(id);
        return membershipType;
    }

    private void setMembershipTypeFields(MembershipType membershipType, PreparedStatement ps) throws SQLException {
        ps.setString(1, membershipType.getName());
        setDurationDays(ps, 2, membershipType);
        setVisitLimit(ps, 3, membershipType);
        ps.setString(4, membershipType.getPrice().toPlainString());
        ps.setString(5, membershipType.getVisitPolicy().name());
        ps.setInt(6, membershipType.isActive() ? 1 : 0);
    }

    @Override
    public Optional<MembershipType> findById(Long id) {
        String sql = "SELECT * FROM membership_types WHERE id = ?";

        return query(sql,
                ps -> ps.setLong(1, id),
                this::mapMembershipType
        ).stream().findFirst();
    }

    @Override
    public List<MembershipType> findAll() {
        return query(
                "SELECT * FROM membership_types ORDER BY id",
                null,
                this::mapMembershipType
        );
    }

    @Override
    public List<MembershipType> findActive() {
        return query(
                "SELECT * FROM membership_types WHERE active = 1 ORDER BY id",
                null,
                this::mapMembershipType
        );
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

        int updated = update(sql, ps -> {
            setMembershipTypeFields(membershipType, ps);
            ps.setLong(7, membershipType.getId());
        });

        if (updated == 0) {
            throw new RuntimeException("Membership type not found: " + membershipType.getId());
        }
    }

    @Override
    public void deactivate(Long id) {
        int updated = update(
                "UPDATE membership_types SET active = 0 WHERE id = ?",
                ps -> ps.setLong(1, id)
        );

        if (updated == 0) {
            throw new RuntimeException("Membership type not found: " + id);
        }
    }

    @Override
    public void reactivate(Long id) {
        int updated = update(
                "UPDATE membership_types SET active = 1 WHERE id = ?",
                ps -> ps.setLong(1, id)
        );

        if (updated == 0) {
            throw new RuntimeException("Membership type not found: " + id);
        }
    }

    @Override
    public Optional<MembershipType> findByName(String name) {
        String sql = """
            SELECT * FROM membership_types
            WHERE LOWER(name) = LOWER(?)
            LIMIT 1
            """;

        return query(sql,
                ps -> ps.setString(1, name.trim()),
                this::mapMembershipType
        ).stream().findFirst();
    }

    private void setDurationDays(PreparedStatement ps, int index, MembershipType membershipType) throws SQLException {
        setIntegerOrNull(ps, index, membershipType.getDurationDays());
    }

    private void setVisitLimit(PreparedStatement ps, int index, MembershipType membershipType) throws SQLException {
        if (membershipType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            setIntegerOrNull(ps, index, membershipType.getVisitLimit());
            return;
        }

        ps.setNull(index, Types.INTEGER);
    }

    private MembershipType mapMembershipType(ResultSet rs) throws SQLException {
        MembershipType membershipType = new MembershipType();

        membershipType.setId(rs.getLong("id"));
        membershipType.setName(rs.getString("name"));
        membershipType.setDurationDays(getIntegerOrNull(rs, "duration_days"));
        membershipType.setVisitLimit(getIntegerOrNull(rs, "visit_limit"));
        membershipType.setPrice(new BigDecimal(rs.getString("price")));
        membershipType.setVisitPolicy(VisitPolicy.valueOf(rs.getString("visit_policy")));
        membershipType.setActive(rs.getInt("active") == 1);

        return membershipType;
    }
}