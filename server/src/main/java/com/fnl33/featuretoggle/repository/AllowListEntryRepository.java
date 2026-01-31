package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.AllowListEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AllowListEntryRepository extends JpaRepository<AllowListEntry, UUID> {
    Page<AllowListEntry> findByToggle_Name(String toggleName, Pageable pageable);
    Optional<AllowListEntry> findByToggle_NameAndValue(String toggleName, String value);
    boolean existsByToggle_NameAndValue(String toggleName, String value);
}
