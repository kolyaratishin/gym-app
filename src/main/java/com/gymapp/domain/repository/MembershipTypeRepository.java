package com.gymapp.domain.repository;

import com.gymapp.domain.membership.MembershipType;

import java.util.List;
import java.util.Optional;

public interface MembershipTypeRepository {

    MembershipType save(MembershipType membershipType);

    Optional<MembershipType> findById(Long id);

    List<MembershipType> findAll();

    List<MembershipType> findActive();

    void update(MembershipType membershipType);

    void deactivate(Long id);

    void reactivate(Long id);

    Optional<MembershipType> findByName(String name);
}
