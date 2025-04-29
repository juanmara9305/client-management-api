package com.client.client_management_api.service;

import com.client.client_management_api.dto.ClientDto;
import com.client.client_management_api.model.Client;
import com.client.client_management_api.repository.IClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientServiceTests {

    private IClientRepository clientRepository;
    private ClientService clientService;

    @BeforeEach
    public void setup() {
        clientRepository = mock(IClientRepository.class);
        clientService = new ClientService(clientRepository);
    }

    @Test
    public void getAllClients_shouldReturnList() {
        Client client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setPhone("123456789");
        client.setEmail("john@example.com");
        client.setDateAdded("01/01/2024");
        client.setSharedKey("jdoe");

        when(clientRepository.findAll()).thenReturn(Collections.singletonList(client));

        var result = clientService.getAllClients(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    public void createClient_shouldCreateSuccessfully() {
        ClientDto inputDto = new ClientDto();
        inputDto.setName("Jane Smith");
        inputDto.setPhone("987654321");
        inputDto.setEmail("jane@example.com");

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);

        when(clientRepository.findBySharedKey("jsmith")).thenReturn(Optional.empty());

        Client savedClient = new Client();
        savedClient.setId(1L);
        savedClient.setName("Jane Smith");
        savedClient.setPhone("987654321");
        savedClient.setEmail("jane@example.com");
        savedClient.setDateAdded(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        savedClient.setSharedKey("jsmith");

        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        ClientDto result = clientService.createClient(inputDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSharedKey()).isEqualTo("jsmith");
        assertThat(result.getName()).isEqualTo("Jane Smith");

        verify(clientRepository).save(clientCaptor.capture());
        assertThat(clientCaptor.getValue().getSharedKey()).isEqualTo("jsmith");
    }

    @Test
    public void createClient_shouldThrowIfNameInvalid() {
        ClientDto dto = new ClientDto();
        dto.setName("SingleName");
        dto.setPhone("123456789");
        dto.setEmail("email@example.com");

        assertThatThrownBy(() -> clientService.createClient(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El nombre debe contener al menos nombre y apellido");
    }

    @Test
    public void createClient_shouldThrowIfSharedKeyExists() {
        ClientDto dto = new ClientDto();
        dto.setName("John Doe");
        dto.setPhone("123456789");
        dto.setEmail("john@example.com");

        Client existing = new Client();
        existing.setId(99L);

        when(clientRepository.findBySharedKey("jdoe")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> clientService.createClient(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un cliente con la sharedKey");
    }

    @Test
    public void updateClient_shouldUpdateSuccessfully() {
        Long clientId = 1L;

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setName("Old Name");
        existingClient.setEmail("old@example.com");
        existingClient.setPhone("000000");
        existingClient.setSharedKey("oldkey");

        ClientDto updateDto = new ClientDto();
        updateDto.setName("New Name");
        updateDto.setEmail("new@example.com");
        updateDto.setPhone("111111");
        updateDto.setSharedKey("newkey");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findBySharedKey("newkey")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        ClientDto result = clientService.updateClient(clientId, updateDto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getSharedKey()).isEqualTo("newkey");
    }

    @Test
    public void updateClient_shouldThrowIfNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        ClientDto updateDto = new ClientDto();
        updateDto.setSharedKey("somekey");

        assertThatThrownBy(() -> clientService.updateClient(1L, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client with ID 1 not found");
    }

    @Test
    public void updateClient_shouldThrowIfSharedKeyUsedByOther() {
        Long targetId = 1L;

        Client existingClient = new Client();
        existingClient.setId(targetId);

        Client anotherClient = new Client();
        anotherClient.setId(99L); // different ID but same shared key

        ClientDto updateDto = new ClientDto();
        updateDto.setSharedKey("conflictkey");

        when(clientRepository.findById(targetId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findBySharedKey("conflictkey")).thenReturn(Optional.of(anotherClient));

        assertThatThrownBy(() -> clientService.updateClient(targetId, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SharedKey 'conflictkey' is already in use.");
    }
}
