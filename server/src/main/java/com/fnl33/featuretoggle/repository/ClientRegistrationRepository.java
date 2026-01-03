package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.ClientRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRegistrationRepository extends JpaRepository<ClientRegistration, UUID> {
}
