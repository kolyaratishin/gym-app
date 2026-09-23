package com.gymapp.telegram.db;

import java.time.LocalDateTime;

public class TelegramNotification {

    private Long id;
    private Long clientId;
    private Long membershipId;
    private TelegramNotificationType type;
    private String referenceValue;
    private LocalDateTime sentAt;

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

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public TelegramNotificationType getType() {
        return type;
    }

    public void setType(TelegramNotificationType type) {
        this.type = type;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}