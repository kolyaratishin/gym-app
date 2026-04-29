package com.gymapp.dashboard.dto;

public class DashboardStats {

    private final long totalClients;
    private final long activeClients;
    private final long visitsToday;
    private final long expiringMemberships;

    public DashboardStats(long totalClients, long activeClients, long visitsToday, long expiringMemberships) {
        this.totalClients = totalClients;
        this.activeClients = activeClients;
        this.visitsToday = visitsToday;
        this.expiringMemberships = expiringMemberships;
    }

    public long getTotalClients() {
        return totalClients;
    }

    public long getActiveClients() {
        return activeClients;
    }

    public long getVisitsToday() {
        return visitsToday;
    }

    public long getExpiringMemberships() {
        return expiringMemberships;
    }
}