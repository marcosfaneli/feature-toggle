package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute, String> {
    Optional<Attribute> findByName(String name);
    boolean existsByName(String name);
}
