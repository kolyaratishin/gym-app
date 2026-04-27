package com.gymapp.domain.repository;

import com.gymapp.domain.sale.Sale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository {

    Sale save(Sale sale);

    Optional<Sale> findById(Long id);

    List<Sale> findAll();

    List<Sale> findByPeriod(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
