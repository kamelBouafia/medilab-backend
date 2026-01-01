package com.medilab.controller;

import com.medilab.dto.GlobalTestCatalogDto;
import com.medilab.service.GlobalTestCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/global-tests")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class GlobalTestCatalogController {

    private final GlobalTestCatalogService globalTestCatalogService;

    @GetMapping
    public ResponseEntity<Page<GlobalTestCatalogDto>> getTests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(globalTestCatalogService.getTests(page, limit, q, category));
    }

    @PostMapping
    public ResponseEntity<GlobalTestCatalogDto> addTest(@Valid @RequestBody GlobalTestCatalogDto dto) {
        return new ResponseEntity<>(globalTestCatalogService.addTest(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalTestCatalogDto> updateTest(@PathVariable Long id,
            @Valid @RequestBody GlobalTestCatalogDto dto) {
        return ResponseEntity.ok(globalTestCatalogService.updateTest(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long id) {
        globalTestCatalogService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }
}
