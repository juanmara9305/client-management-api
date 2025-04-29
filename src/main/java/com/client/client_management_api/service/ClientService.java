package com.client.client_management_api.service;

import com.client.client_management_api.dto.ClientDto;
import com.client.client_management_api.model.Client;
import com.client.client_management_api.repository.IClientRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ClientService {

    private final IClientRepository clientRepository;
    private final DateTimeFormatter isoDateFmt    = DateTimeFormatter.ISO_LOCAL_DATE;

    Logger logger = LoggerFactory.getLogger(ClientService.class);

    public ClientService(IClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientDto> getAllClients(
    		Optional<String> sharedKey, 
    		Optional<String> name, 
    		Optional<String> phone, 
    		Optional<String> email, 
    		Optional<String> startDate, 
    		Optional<String> endDate) {
    	  Optional<LocalDate> start = (startDate.isEmpty() || startDate.get().isEmpty()) ? Optional.empty(): startDate.map(isoDateFmt::parse).map(LocalDate::from);
    	  Optional<LocalDate> end = (endDate.isEmpty() || endDate.get().isEmpty()) ? Optional.empty(): endDate.map(isoDateFmt::parse).map(LocalDate::from);
 
          Specification<Client> spec = Specification.where(null);

          if (sharedKey.isPresent() && !sharedKey.get().isEmpty()) {
              String key = sharedKey.get().toLowerCase();
              spec = spec.and((root, cq, cb) ->
                  cb.equal(cb.lower(root.get("sharedKey")), key)
              );
          }
          if (name.isPresent()&& !name.get().isEmpty()) {
              String n = "%" + name.get().toLowerCase() + "%";
              spec = spec.and((root, cq, cb) ->
                  cb.like(cb.lower(root.get("name")), n)
              );
          }
          if (phone.isPresent()&& !phone.get().isEmpty()) {
              spec = spec.and((root, cq, cb) ->
                  cb.like(root.get("phone"), "%" + phone.get() + "%")
              );
          }
          if (email.isPresent()&& !email.get().isEmpty()) {
              String e = "%" + email.get().toLowerCase() + "%";
              spec = spec.and((root, cq, cb) ->
                  cb.like(cb.lower(root.get("email")), e)
              );
          }
          if ((startDate.isPresent() && !startDate.get().isEmpty()) || (endDate.isPresent() && !endDate.get().isEmpty())) {
        	    spec = spec.and((root, cq, cb) -> {
        	        Expression<String> addedDateStr = root.get("dateAdded");
        	        Expression<java.sql.Date> addedDate = cb.function(
        	            "strftime",
        	            java.sql.Date.class,
        	            cb.literal("%Y-%m-%d"),
        	            cb.function("substr", String.class, addedDateStr, cb.literal(7), cb.literal(4)), // year
        	            cb.literal("-"),
        	            cb.function("substr", String.class, addedDateStr, cb.literal(4), cb.literal(2)), // month
        	            cb.literal("-"),
        	            cb.function("substr", String.class, addedDateStr, cb.literal(1), cb.literal(2))  // day
        	        );

        	        Predicate afterStart = startDate
        	            .filter(s -> !s.isEmpty())
        	            .map(s -> cb.greaterThanOrEqualTo(addedDate, java.sql.Date.valueOf(LocalDate.parse(s))))
        	            .orElse(cb.conjunction());

        	        Predicate beforeEnd = endDate
        	            .filter(e -> !e.isEmpty())
        	            .map(e -> cb.lessThanOrEqualTo(addedDate, java.sql.Date.valueOf(LocalDate.parse(e))))
        	            .orElse(cb.conjunction());

        	        return cb.and(afterStart, beforeEnd);
        	    });
        	}

          List<Client> clients = clientRepository.findAll(spec);

          return clients.stream()
                        .map(ClientDto::fromEntity)
                        .collect(Collectors.toList());
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
