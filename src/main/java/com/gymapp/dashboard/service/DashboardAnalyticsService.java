package com.gymapp.dashboard.service;

import com.gymapp.dashboard.db.DashboardAnalyticsRepository;
import com.gymapp.dashboard.dto.ClientVisitStat;
import com.gymapp.dashboard.dto.VisitDayStat;
import java.time.LocalDate;
import java.util.List;

public class DashboardAnalyticsService {

    private final DashboardAnalyticsRepository repository;

    public DashboardAnalyticsService(DashboardAnalyticsRepository repository) {
        this.repository = repository;
    }

    public List<VisitDayStat> getVisitsByDayLast30Days() {
        return repository.findVisitsByDayFrom(LocalDate.now().minusDays(29));
    }

    public List<ClientVisitStat> getTopClientsByVisits(int limit) {
        return repository.findTopClientsByVisits(limit);
    }

    public List<ClientVisitStat> getActiveClientsWithoutVisits() {
        return repository.findActiveClientsWithoutVisits();
    }
}