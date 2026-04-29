package com.gymapp.app.dashboard;

import java.time.LocalDate;

public class VisitDayStat {

    private final LocalDate day;
    private final long visits;

    public VisitDayStat(LocalDate day, long visits) {
        this.day = day;
        this.visits = visits;
    }

    public LocalDate getDay() {
        return day;
    }

    public long getVisits() {
        return visits;
    }
}