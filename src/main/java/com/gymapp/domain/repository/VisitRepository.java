package com.gymapp.domain.repository;

import com.gymapp.domain.visit.Visit;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitRepository {

    Visit save(Visit visit);

    Optional<Visit> findById(Long clientId);

    List<Visit> findByMembershipId(Long membershipId);

    List<Visit> findByDate(LocalDate date);

    List<Visit> findAll();

    List<Visit> findByClientId(Long clientId);

    long countByDate(LocalDate date);
}
