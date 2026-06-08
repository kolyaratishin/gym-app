package com.gymapp.membership.service;

import com.gymapp.audit.ActivityLogger;
import com.gymapp.audit.AuditEventType;
import com.gymapp.audit.AuditLogMessages;
import com.gymapp.membership.db.MembershipTypeRepository;
import com.gymapp.membership.db.domain.MembershipType;

import java.util.List;
import java.util.Optional;

public class MembershipTypeService {

    private final MembershipTypeRepository membershipTypeRepository;

    public MembershipTypeService(MembershipTypeRepository membershipTypeRepository) {
        this.membershipTypeRepository = membershipTypeRepository;
    }

    public List<MembershipType> findAll() {
        return membershipTypeRepository.findAll();
    }

    public List<MembershipType> findActive() {
        return membershipTypeRepository.findActive();
    }

    public Optional<MembershipType> findById(Long id) {
        return membershipTypeRepository.findById(id);
    }

    public MembershipType save(MembershipType membershipType) {
        MembershipType saved = membershipTypeRepository.save(membershipType);

        ActivityLogger.log(
                AuditEventType.MEMBERSHIP_TYPE_CREATED,
                AuditLogMessages.membershipTypeCreated(saved)
        );

        return saved;
    }

    public void update(MembershipType membershipType) {
        membershipTypeRepository.update(membershipType);

        ActivityLogger.log(
                AuditEventType.MEMBERSHIP_TYPE_UPDATED,
                AuditLogMessages.membershipTypeUpdated(membershipType)
        );
    }

    public void deactivate(Long id) {
        membershipTypeRepository.deactivate(id);

        ActivityLogger.log(
                AuditEventType.MEMBERSHIP_TYPE_DEACTIVATED,
                AuditLogMessages.membershipTypeDeactivated(id)
        );
    }

    public void reactivate(Long id) {
        membershipTypeRepository.reactivate(id);

        ActivityLogger.log(
                AuditEventType.MEMBERSHIP_TYPE_REACTIVATED,
                AuditLogMessages.membershipTypeReactivated(id)
        );
    }
}
