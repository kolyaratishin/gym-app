package com.gymapp.membership.db.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Membership {

    private Long id;
    private Long clientId;
    private Long membershipTypeId;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer remainingVisits;

    private MembershipStatus status;

    private LocalDateTime pausedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getMembershipTypeId() {
        return membershipTypeId;
    }

    public void setMembershipTypeId(Long membershipTypeId) {
        this.membershipTypeId = membershipTypeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getRemainingVisits() {
        return remainingVisits;
    }

    public void setRemainingVisits(Integer remainingVisits) {
        this.remainingVisits = remainingVisits;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(LocalDateTime pausedAt) {
        this.pausedAt = pausedAt;
    }
}