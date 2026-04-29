package com.gymapp.membership.db.domain;

import java.math.BigDecimal;

public class MembershipType {

    private Long id;
    private String name;
    private Integer durationDays;
    private Integer visitLimit;
    private BigDecimal price;
    private VisitPolicy visitPolicy;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getVisitLimit() {
        return visitLimit;
    }

    public void setVisitLimit(Integer visitLimit) {
        this.visitLimit = visitLimit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public VisitPolicy getVisitPolicy() {
        return visitPolicy;
    }

    public void setVisitPolicy(VisitPolicy visitPolicy) {
        this.visitPolicy = visitPolicy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isUnlimited() {
        return visitPolicy == VisitPolicy.UNLIMITED;
    }

    public boolean isLimitedByVisits() {
        return visitPolicy == VisitPolicy.LIMITED_BY_VISITS;
    }

    public boolean isLimitedByTime() {
        return visitPolicy == VisitPolicy.LIMITED_BY_TIME;
    }
}
