package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.Toggle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ToggleRepository extends JpaRepository<Toggle, String> {
    Optional<Toggle> findByName(String name);
    boolean existsByName(String name);
    boolean existsByAttribute_Name(String attributeName);
    List<Toggle> findByAttribute_Name(String attributeName);
}
