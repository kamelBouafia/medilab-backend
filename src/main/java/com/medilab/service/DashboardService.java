package com.medilab.service;

import com.medilab.dto.DashboardStatsDto;
import com.medilab.dto.DailyVolumeDto;
import com.medilab.entity.SampleStatus;
import com.medilab.repository.InventoryRepository;
import com.medilab.repository.RequisitionRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import com.medilab.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final RequisitionRepository requisitionRepository;
        private final InventoryRepository inventoryRepository;

        public DashboardStatsDto getDashboardStats() {
                AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
                Long labId = user.getLabId();

                long pendingTests = requisitionRepository.countByHierarchicalLabIdAndStatusIn(labId,
                                Arrays.asList(SampleStatus.COLLECTED, SampleStatus.IN_TRANSIT,
                                                SampleStatus.PROCESSING));

                long completedToday = requisitionRepository.countByHierarchicalLabIdAndStatusAndCompletionDateBetween(
                                labId,
                                SampleStatus.COMPLETED,
                                LocalDate.now().atStartOfDay(),
                                LocalDate.now().atTime(LocalTime.MAX));

                long lowStockItems = inventoryRepository
                                .countByHierarchicalLabIdAndQuantityLessThanLowStockThreshold(labId);

                // Calculate date range for last 7 days (inclusive of today)
                // Ensure we work with OffsetDateTime for the repository query
                OffsetDateTime end = OffsetDateTime.now();
                OffsetDateTime start = end.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);

                List<DailyVolumeDto> volume = requisitionRepository.findHierarchicalDailyRequestVolume(labId,
                                start, end);

                // Fill in missing days with 0
                List<DailyVolumeDto> fullWeek = new ArrayList<>();
                Map<LocalDate, Long> volumeMap = volume.stream()
                                .collect(Collectors.toMap(DailyVolumeDto::getDate,
                                                DailyVolumeDto::getCount));

                for (int i = 0; i < 7; i++) {
                        LocalDate date = start.toLocalDate().plusDays(i);
                        fullWeek.add(new DailyVolumeDto(date, volumeMap.getOrDefault(date, 0L)));
                }

                return new DashboardStatsDto(pendingTests, completedToday, lowStockItems, fullWeek);
        }
}
