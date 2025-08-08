package com.dwclm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMetricsDTO {
    private long totalUsers;
    private long breachedUsers;
    private long totalBreaches;
    private long uniqueBreachNames;
}
