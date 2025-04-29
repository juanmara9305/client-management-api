package com.client.client_management_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.client.client_management_api.model.Client;

public interface IClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
	   Optional<Client> findBySharedKey(String sharedKey);
}
