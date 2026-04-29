package com.gymapp.client.db;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {

    Client save(Client client);

    Optional<Client> findById(Long id);

    List<Client> findAll();

    List<Client> search(String query);

    void update(Client client);

    void deactivate(Long id);

    void reactivate(Long clientId);

    long countAll();

    long countActive();

    boolean existsByClientNumber(Integer clientNumber);
}
