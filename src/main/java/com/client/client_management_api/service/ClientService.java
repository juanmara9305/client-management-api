package com.client.client_management_api.service;
import org.springframework.stereotype.Service;

import com.client.client_management_api.model.Client;
import com.client.client_management_api.repository.IClientRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final IClientRepository clientRepository;

    public ClientService(IClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client createClient(Client client) {
        String[] nameParts = client.getName().split(" ");
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("El nombre debe contener al menos nombre y apellido");
        }

        String firstName = nameParts[0];
        String lastName = nameParts[nameParts.length - 1];
        String sharedKey = (firstName.charAt(0) + lastName).toLowerCase();

        Optional<Client> existingClient = clientRepository.findBySharedKey(sharedKey);
        if (existingClient.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con la sharedKey: " + sharedKey);
        }

        client.setSharedKey(sharedKey);
        return clientRepository.save(client);
    }

    public Client updateClient(Long id, Client updatedClient) {
        Client existingClient = clientRepository.findById(id).orElse(null);
        if (existingClient == null) {
            throw new RuntimeException("Client with ID " + id + " not found.");
        }
        Client clientWithSameSharedKey = clientRepository.findBySharedKey(updatedClient.getSharedKey()).orElse(null);
        if (clientWithSameSharedKey != null && !id.equals(clientWithSameSharedKey.getId())) {
            throw new RuntimeException("SharedKey '" + updatedClient.getSharedKey() + "' is already in use.");
        }

        existingClient.setName(updatedClient.getName());
        existingClient.setEmail(updatedClient.getEmail());
        existingClient.setPhone(updatedClient.getPhone());
        existingClient.setSharedKey(updatedClient.getSharedKey());
        
        return clientRepository.save(existingClient);
    }
  
}
