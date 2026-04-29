package com.gymapp.app.client;

import com.gymapp.domain.client.Client;
import com.gymapp.domain.repository.ClientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public List<Client> search(String query) {
        if (query == null || query.isBlank()) {
            return clientRepository.findAll();
        }
        return clientRepository.search(query);
    }

    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    public void deactivate(Long clientId) {
        clientRepository.deactivate(clientId);
    }

    public void reactivate(Long clientId) {
        clientRepository.reactivate(clientId);
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public void update(Client client) {
        clientRepository.update(client);
    }

    public Client createEmptyClient(Integer clientNumber) {
        Client client = new Client();
        client.setClientNumber(clientNumber);
        client.setFirstName("");
        client.setLastName("");
        client.setPhone(null);
        client.setBirthDate(null);
        client.setNotes(null);
        client.setRegistrationDate(LocalDate.now());
        client.setActive(false);

        return clientRepository.save(client);
    }
}