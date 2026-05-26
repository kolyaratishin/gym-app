package com.gymapp.client.dto;

import com.gymapp.membership.db.domain.VisitPolicy;

import java.time.LocalDate;

public class ClientTableRow {

    private Long id;
    private Integer clientNumber;
    private String firstName;
    private String lastName;
    private String notes;
    private boolean active;

    private String membershipName;
    private LocalDate membershipEndDate;
    private VisitPolicy visitPolicy;

    public ClientTableRow(
            Long id,
            Integer clientNumber,
            String firstName,
            String lastName,
            String notes,
            boolean active,
            String membershipName,
            LocalDate membershipEndDate,
            VisitPolicy visitPolicy
    ) {
        this.id = id;
        this.clientNumber = clientNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.notes = notes;
        this.active = active;
        this.membershipName = membershipName;
        this.membershipEndDate = membershipEndDate;
        this.visitPolicy = visitPolicy;
    }

    public Long getId() { return id; }
    public Integer getClientNumber() { return clientNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
    public String getMembershipName() { return membershipName; }
    public LocalDate getMembershipEndDate() { return membershipEndDate; }
    public VisitPolicy getVisitPolicy() { return visitPolicy; }
}