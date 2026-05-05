package com.gymapp.client.service;

import com.gymapp.client.db.Client;
import com.gymapp.client.db.ClientRepository;

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

    public List<Client> search(String input) {
        String trimmed = input == null ? "" : input.trim();

        if (trimmed.isEmpty()) {
            return findAll();
        }

        // 🔥 1. пробуємо як number
        if (isInteger(trimmed)) {
            List<Client> byNumber = clientRepository.findByClientNumber(Integer.parseInt(trimmed));

            if (!byNumber.isEmpty()) {
                return byNumber;
            }
        }

        // 🔥 2. fallback на текст
        return clientRepository.search(trimmed);
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

    public boolean existsByClientNumber(Integer clientNumber) {
        return clientRepository.existsByClientNumber(clientNumber);
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Client createEmptyClient(Integer clientNumber) {
        if (clientRepository.existsByClientNumber(clientNumber)) {
            throw new RuntimeException("Клієнт з номером " + clientNumber + " вже існує");
        }
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