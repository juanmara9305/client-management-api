package com.client.client_management_api.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.client.client_management_api.model.Client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class ClientDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(
        regexp = "^\\S+\\s+\\S+(?:.*)$",
        message = "El nombre debe contener al menos nombre y apellido"
    )
    private String name;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    private String dateAdded;
    private String sharedKey;

    public ClientDto() {}

    public static ClientDto fromEntity(Client client) {
    	if (client == null) {
    		throw new IllegalArgumentException("Client cannot be null");
	    }
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setPhone(client.getPhone());
        dto.setEmail(client.getEmail());
        dto.setDateAdded(client.getDateAdded());
        dto.setSharedKey(client.getSharedKey());
        return dto;
    }

    public Client toEntity() {
        Client client = new Client();
        client.setId(this.id);
        client.setName(this.name);
        client.setPhone(this.phone);
        client.setEmail(this.email);
        return client;
    }
    
    public static List<ClientDto> fromEntities(List<Client> clients) {
        return clients.stream()
                      .map(ClientDto::fromEntity)
                      .collect(Collectors.toList());
    }

    public static List<Client> toEntities(List<ClientDto> dtos) {
        return dtos.stream()
                   .map(ClientDto::toEntity)
                   .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    private void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }
}
