package com.client.client_management_api.controller;

import org.springframework.web.bind.annotation.*;

import com.client.client_management_api.dto.ClientDto;
import com.client.client_management_api.service.ClientService;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@RequestMapping("/api/clients")
public class ClientController {
	
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientDto> getClients() {
        return clientService.getAllClients();
    }

    @PostMapping
    public ClientDto addClient(@RequestBody ClientDto client) {
    	return clientService.createClient(client);
    }

    @PutMapping("/{id}")
    public ClientDto updateClient(@PathVariable Long id, @RequestBody ClientDto client) {
        return clientService.updateClient(id, client);
    }
    
}
