package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.AllowListEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AllowListEntryRepository extends JpaRepository<AllowListEntry, UUID> {
}
