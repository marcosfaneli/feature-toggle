package com.fnl33.featuretoggle.controller;

import com.fnl33.featuretoggle.dto.ToggleDetailResponse;
import com.fnl33.featuretoggle.dto.PagedResponse;
import com.fnl33.featuretoggle.domain.Toggle;
import com.fnl33.featuretoggle.service.ToggleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/toggles/{name}/allow-list")
public class AllowListController {

    private static final Logger logger = LoggerFactory.getLogger(AllowListController.class);
    private final ToggleService toggleService;

    public AllowListController(ToggleService toggleService) {
        this.toggleService = toggleService;
    }

    @PostMapping
    public ResponseEntity<ToggleDetailResponse> addToAllowList(
            @PathVariable String name,
            @RequestParam String value) {

        logger.info("Adding value to allow list for toggle name: {}", name);

        toggleService.addAllowListEntry(name, value);
        final Toggle toggle = toggleService.findByName(name);
        final ToggleDetailResponse response = ToggleDetailResponse.from(toggle);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ToggleDetailResponse> removeFromAllowList(
            @PathVariable String name,
            @RequestParam String value) {

        logger.info("Removing value from allow list for toggle name: {}", name);

        toggleService.removeAllowListEntry(name, value);
        final Toggle toggle = toggleService.findByName(name);
        final ToggleDetailResponse response = ToggleDetailResponse.from(toggle);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<String>> getAllowList(@PathVariable String name, Pageable pageable) {
        logger.debug("Fetching allow list for toggle name: {}", name);

        final var pagedAllowList = toggleService.findAllowListValues(name, pageable);
        final PagedResponse<String> allowList = PagedResponse.from(pagedAllowList);

        return ResponseEntity.ok(allowList);
    }
}
