package com.medilab.repository;

import com.medilab.entity.AgreementTestPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgreementTestPriceRepository extends JpaRepository<AgreementTestPrice, Long> {

    List<AgreementTestPrice> findByAgreementId(Long agreementId);

    void deleteByAgreementId(Long agreementId);
}
