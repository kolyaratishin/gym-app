package com.gymapp.audit.dto;

public class ActivityLogEntry {

    private final String time;
    private final String eventType;
    private final String eventLabel;
    private final String description;

    public ActivityLogEntry(String time, String eventType, String eventLabel, String description) {
        this.time = time;
        this.eventType = eventType;
        this.eventLabel = eventLabel;
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public String getDescription() {
        return description;
    }
}
