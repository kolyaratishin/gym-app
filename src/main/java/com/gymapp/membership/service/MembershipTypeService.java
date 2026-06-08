package com.gymapp.membership.service;

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
        return membershipTypeRepository.save(membershipType);
    }

    public void update(MembershipType membershipType) {
        membershipTypeRepository.update(membershipType);
    }

    public void deactivate(Long id) {
        membershipTypeRepository.deactivate(id);
    }

    public void reactivate(Long id) {
        membershipTypeRepository.reactivate(id);
    }
}