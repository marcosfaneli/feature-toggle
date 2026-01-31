package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.ClientRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ClientRegistrationRepository extends JpaRepository<ClientRegistration, UUID> {
    @EntityGraph(attributePaths = {"toggles"})
    Page<ClientRegistration> findAll(Pageable pageable);
    List<ClientRegistration> findByTogglesContains(String toggleName);
}
