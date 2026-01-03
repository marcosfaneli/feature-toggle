package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.AllowListEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AllowListEntryRepository extends JpaRepository<AllowListEntry, UUID> {
    Optional<AllowListEntry> findByToggle_NameAndValue(String toggleName, String value);
    boolean existsByToggle_NameAndValue(String toggleName, String value);
}
