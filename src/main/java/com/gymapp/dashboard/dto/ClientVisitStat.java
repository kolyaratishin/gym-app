package com.gymapp.dashboard.dto;

public class ClientVisitStat {

    private final Integer clientNumber;
    private final String fullName;
    private final long visits;

    public ClientVisitStat(Integer clientNumber, String fullName, long visits) {
        this.clientNumber = clientNumber;
        this.fullName = fullName;
        this.visits = visits;
    }

    public Integer getClientNumber() {
        return clientNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public long getVisits() {
        return visits;
    }
}