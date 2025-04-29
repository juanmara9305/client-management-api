package com.client.client_management_api.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.client.client_management_api.dto.ClientDto;
import com.client.client_management_api.model.Client;
import com.client.client_management_api.repository.IClientRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@Service
public class ClientService {

    private final IClientRepository clientRepository;
    Logger logger = LoggerFactory.getLogger(ClientService.class);

    public ClientService(IClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientDto> getAllClients() {
        return ClientDto.fromEntities(clientRepository.findAll());
    }

    public ClientDto createClient(ClientDto clientDto) {
        Client client = clientDto.toEntity();
        String[] nameParts = client.getName().split(" ");
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("El nombre debe contener al menos nombre y apellido");
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateAdded = LocalDate.now().format(fmt);

        client.setDateAdded(dateAdded);

        String firstName = nameParts[0];
        String lastName = nameParts[nameParts.length - 1];
        String sharedKey = (firstName.charAt(0) + lastName).toLowerCase();

        Optional<Client> existingClient = clientRepository.findBySharedKey(sharedKey);
        if (existingClient.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con la sharedKey: " + sharedKey);
        }
        logger.info("Using shared key " + sharedKey);
        client.setSharedKey(sharedKey);
        logger.info("Using date Added " + dateAdded);
        return ClientDto.fromEntity(clientRepository.save(client));
    }

    public ClientDto updateClient(Long id, ClientDto updatedClient) {
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
        
        return ClientDto.fromEntity(clientRepository.save(existingClient));
    }
  
}
