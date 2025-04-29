package com.client.client_management_api.service;

import com.client.client_management_api.model.Client;
import com.client.client_management_api.repository.IClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private IClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    private Client sampleClient;

    @BeforeEach
    void setUp() {
        sampleClient = new Client();
        sampleClient.setId(1L);
        sampleClient.setName("John Doe");
        sampleClient.setEmail("john.doe@example.com");
        sampleClient.setPhone("1234567890");
        sampleClient.setSharedKey("jdoe");
    }

    @Test
    void getAllClients_returnsListFromRepository() {
        List<Client> expected = Arrays.asList(sampleClient);
        when(clientRepository.findAll()).thenReturn(expected);

        List<Client> actual = clientService.getAllClients();

        assertSame(expected, actual);
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void getAllClients_emptyList() {
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        List<Client> actual = clientService.getAllClients();

        assertTrue(actual.isEmpty());
        verify(clientRepository).findAll();
    }

    @Test
    void createClient_validName_savesAndReturnsClientWithGeneratedSharedKey() {
        Client toCreate = new Client();
        toCreate.setName("Alice Smith");
        toCreate.setEmail("alice@example.com");
        toCreate.setPhone("5551234");

        when(clientRepository.findBySharedKey("asmith")).thenReturn(Optional.empty());
        Client saved = new Client();
        saved.setId(2L);
        saved.setName(toCreate.getName());
        saved.setEmail(toCreate.getEmail());
        saved.setPhone(toCreate.getPhone());
        saved.setSharedKey("asmith");
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        Client result = clientService.createClient(toCreate);

        verify(clientRepository).findBySharedKey("asmith");
        verify(clientRepository).save(clientCaptor.capture());

        Client passedToSave = clientCaptor.getValue();
        assertEquals("asmith", passedToSave.getSharedKey());
        assertEquals("Alice Smith", passedToSave.getName());
        assertEquals("alice@example.com", passedToSave.getEmail());
        assertEquals("5551234", passedToSave.getPhone());

        assertEquals(2L, result.getId());
        assertEquals("asmith", result.getSharedKey());
    }

    @Test
    void createClient_nameWithoutSpace_throwsIllegalArgumentException() {
        Client bad = new Client();
        bad.setName("SingleName");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clientService.createClient(bad));
        assertEquals("El nombre debe contener al menos nombre y apellido", ex.getMessage());
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void createClient_duplicateSharedKey_throwsIllegalArgumentException() {
        Client toCreate = new Client();
        toCreate.setName("Bob Brown");
        when(clientRepository.findBySharedKey("bbrown"))
                .thenReturn(Optional.of(sampleClient));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clientService.createClient(toCreate));
        assertTrue(ex.getMessage().contains("Ya existe un cliente con la sharedKey: bbrown"));
        verify(clientRepository).findBySharedKey("bbrown");
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClient_nonExistentId_throwsRuntimeException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> clientService.updateClient(99L, sampleClient));
        assertEquals("Client with ID 99 not found.", ex.getMessage());
        verify(clientRepository).findById(99L);
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void updateClient_sharedKeyConflict_throwsRuntimeException() {
        Client updated = new Client();
        updated.setSharedKey("jdoe");
        updated.setName("John Doe");
        updated.setEmail("new@example.com");
        updated.setPhone("000");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));
        Client other = new Client();
        other.setId(2L);
        when(clientRepository.findBySharedKey("jdoe"))
                .thenReturn(Optional.of(other));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> clientService.updateClient(1L, updated));
        assertEquals("SharedKey 'jdoe' is already in use.", ex.getMessage());

        verify(clientRepository).findById(1L);
        verify(clientRepository).findBySharedKey("jdoe");
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClient_successfulUpdate_savesAndReturnsUpdatedClient() {
        Client toUpdate = new Client();
        toUpdate.setSharedKey("jsmith");
        toUpdate.setName("Jane Smith");
        toUpdate.setEmail("jane.smith@example.com");
        toUpdate.setPhone("999888777");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));
        when(clientRepository.findBySharedKey("jsmith")).thenReturn(Optional.empty());

        Client saved = new Client();
        saved.setId(1L);
        saved.setSharedKey("jsmith");
        saved.setName("Jane Smith");
        saved.setEmail("jane.smith@example.com");
        saved.setPhone("999888777");
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        Client result = clientService.updateClient(1L, toUpdate);

        verify(clientRepository).findById(1L);
        verify(clientRepository).findBySharedKey("jsmith");
        verify(clientRepository).save(clientCaptor.capture());

        Client passed = clientCaptor.getValue();
        assertEquals(1L, passed.getId());
        assertEquals("Jane Smith", passed.getName());
        assertEquals("jane.smith@example.com", passed.getEmail());
        assertEquals("999888777", passed.getPhone());
        assertEquals("jsmith", passed.getSharedKey());

        assertEquals(saved, result);
    }
}
