package com.gymapp.app.client;

import java.util.List;

public class ImportResult {

    private final int imported;
    private final int membershipsImported;
    private final int skipped;
    private final List<String> errors;

    public ImportResult(int imported, int membershipsImported, int skipped, List<String> errors) {
        this.imported = imported;
        this.membershipsImported = membershipsImported;
        this.skipped = skipped;
        this.errors = errors;
    }

    public int getImported() {
        return imported;
    }

    public int getMembershipsImported() {
        return membershipsImported;
    }

    public int getSkipped() {
        return skipped;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}