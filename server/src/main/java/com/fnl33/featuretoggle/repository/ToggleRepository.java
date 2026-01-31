package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.Toggle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ToggleRepository extends JpaRepository<Toggle, String> {
    @EntityGraph(attributePaths = {"attribute"})
    Optional<Toggle> findByName(String name);

    @EntityGraph(attributePaths = {"attribute"})
    Page<Toggle> findAll(Pageable pageable);
    boolean existsByName(String name);
    boolean existsByAttribute_Name(String attributeName);
    List<Toggle> findByAttribute_Name(String attributeName);
}
