package com.client.client_management_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.client.client_management_api.model.Client;
import com.client.client_management_api.repository.IClientRepository;
import com.client.client_management_api.service.ClientService;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@RequestMapping("/api/clients")
public class ClientController {
	
	@Autowired
    private IClientRepository clientRepository;

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<Client> getClients() {
        return clientService.getAllClients();
    }

    @PostMapping
    public Client addClient(@RequestBody Client client) {
    	Client savedClient = clientRepository.save(client);
    	return savedClient;
    }

    @PutMapping("/{id}")
    public Client updateClient(@PathVariable Long id, @RequestBody Client client) {
        return clientService.updateClient(id, client);
    }
    
}
