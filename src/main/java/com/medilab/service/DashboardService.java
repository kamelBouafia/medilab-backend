package com.medilab.service;

import com.medilab.dto.DashboardStatsDto;
import com.medilab.entity.SampleStatus;
import com.medilab.repository.InventoryRepository;
import com.medilab.repository.RequisitionRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import com.medilab.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RequisitionRepository requisitionRepository;
    private final InventoryRepository inventoryRepository;

    public DashboardStatsDto getDashboardStats() {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Long labId = user.getLabId();

        long pendingTests = requisitionRepository.countByLabIdAndStatusIn(labId,
                Arrays.asList(SampleStatus.COLLECTED, SampleStatus.IN_TRANSIT, SampleStatus.PROCESSING));

        long completedToday = requisitionRepository.countByLabIdAndStatusAndCompletionDateBetween(
                labId,
                SampleStatus.COMPLETED,
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX));

        long lowStockItems = inventoryRepository.countByLabIdAndQuantityLessThanLowStockThreshold(labId);

        return new DashboardStatsDto(pendingTests, completedToday, lowStockItems);
    }
}
