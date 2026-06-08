package com.gymapp.audit.dto;

public class ErrorLogEntry {

    private final String time;
    private final String source;
    private final String type;
    private final String message;
    private final String details;

    public ErrorLogEntry(String time, String source, String type, String message, String details) {
        this.time = time;
        this.source = source;
        this.type = type;
        this.message = message;
        this.details = details;
    }

    public String getTime() {
        return time;
    }

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}
