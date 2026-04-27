package com.gymapp.infrastructure.db;

import org.flywaydb.core.Flyway;

public class FlywayMigrator {

    private final String databaseUrl;

    public FlywayMigrator(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void migrate() {
        Flyway.configure()
                .dataSource(databaseUrl, null, null)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
