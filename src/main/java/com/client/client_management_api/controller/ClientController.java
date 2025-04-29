package com.client.client_management_api.controller;

import org.springframework.web.bind.annotation.*;

import com.client.client_management_api.dto.ClientDto;
import com.client.client_management_api.service.ClientService;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@RequestMapping("/api/clients")
public class ClientController {
	
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientDto> getClients(
    		@RequestParam(name = "sharedKey", required = false) Optional<String> sharedKey, 
    		@RequestParam(name = "name", required = false) Optional<String> name, 
    		@RequestParam(name = "phone", required = false) Optional<String> phone, 
    		@RequestParam(name = "email", required = false) Optional<String> email, 
    		@RequestParam(name = "startDate", required = false) Optional<String> startDate, 
    		@RequestParam(name = "endDate", required = false) Optional<String> endDate) {
        return clientService.getAllClients(sharedKey, name, phone, email, startDate, endDate);
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
