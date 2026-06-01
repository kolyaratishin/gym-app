package com.gymapp.visit.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClientVisitHistoryRow {

    private final Long visitId;
    private final LocalDateTime visitTime;
    private final Long membershipId;
    private final String membershipTypeName;
    private final LocalDate membershipStartDate;
    private final LocalDate membershipEndDate;
    private final Integer remainingVisits;

    public ClientVisitHistoryRow(
            Long visitId,
            LocalDateTime visitTime,
            Long membershipId,
            String membershipTypeName,
            LocalDate membershipStartDate,
            LocalDate membershipEndDate,
            Integer remainingVisits
    ) {
        this.visitId = visitId;
        this.visitTime = visitTime;
        this.membershipId = membershipId;
        this.membershipTypeName = membershipTypeName;
        this.membershipStartDate = membershipStartDate;
        this.membershipEndDate = membershipEndDate;
        this.remainingVisits = remainingVisits;
    }

    public Long getVisitId() { return visitId; }
    public LocalDateTime getVisitTime() { return visitTime; }
    public Long getMembershipId() { return membershipId; }
    public String getMembershipTypeName() { return membershipTypeName; }
    public LocalDate getMembershipStartDate() { return membershipStartDate; }
    public LocalDate getMembershipEndDate() { return membershipEndDate; }
    public Integer getRemainingVisits() { return remainingVisits; }
}