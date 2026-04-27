package com.gymapp.app.config;

public final class AppConfig {

    private final String applicationName;
    private final String databasePath;

    public AppConfig(String applicationName, String databasePath) {
        this.applicationName = applicationName;
        this.databasePath = databasePath;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDatabasePath() {
        return databasePath;
    }
}
