package com.medilab.repository;

import com.medilab.entity.GlobalTestCatalog;
import com.medilab.enums.TestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalTestCatalogRepository extends JpaRepository<GlobalTestCatalog, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<GlobalTestCatalog> {
    Optional<GlobalTestCatalog> findByCode(String code);

    List<GlobalTestCatalog> findByCategory(TestCategory category);
}
