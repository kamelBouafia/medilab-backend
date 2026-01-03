package com.medilab.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemStatsDto {
    private long totalLabs;
    private long activeSubscriptions;
    private long totalUsers;
    private double systemHealth;
}
