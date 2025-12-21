package com.medilab.service;

import com.medilab.dto.GlobalTestCatalogDto;
import com.medilab.entity.GlobalTestCatalog;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.enums.TestCategory;
import com.medilab.mapper.GlobalTestCatalogMapper;
import com.medilab.repository.GlobalTestCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GlobalTestCatalogService {

    private final GlobalTestCatalogRepository globalTestCatalogRepository;
    private final GlobalTestCatalogMapper globalTestCatalogMapper;

    public Page<GlobalTestCatalogDto> getTests(int page, int limit, String q, String category) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("code"));

        Specification<GlobalTestCatalog> spec = Specification.where(null);

        if (StringUtils.hasText(q)) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), "%" + q.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + q.toLowerCase() + "%")));
        }

        if (StringUtils.hasText(category)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), TestCategory.valueOf(category)));
        }

        return globalTestCatalogRepository.findAll(spec, pageable).map(globalTestCatalogMapper::toDto);
    }

    public GlobalTestCatalogDto addTest(GlobalTestCatalogDto dto) {
        GlobalTestCatalog entity = globalTestCatalogMapper.toEntity(dto);
        return globalTestCatalogMapper.toDto(globalTestCatalogRepository.save(entity));
    }

    public GlobalTestCatalogDto updateTest(Long id, GlobalTestCatalogDto dto) {
        GlobalTestCatalog existing = globalTestCatalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        existing.setCode(dto.getCode());
        existing.setCategory(dto.getCategory());
        existing.setDefaultUnit(dto.getDefaultUnit());
        existing.setNames(dto.getNames());
        existing.setDescription(dto.getDescription());

        return globalTestCatalogMapper.toDto(globalTestCatalogRepository.save(existing));
    }

    public void deleteTest(Long id) {
        if (!globalTestCatalogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test not found");
        }
        globalTestCatalogRepository.deleteById(id);
    }
}
