package com.gymapp.infrastructure.settings;

import java.util.Optional;

public interface AppSettingsRepository {

    void save(String key, String value);

    Optional<String> findByKey(String key);

    void delete(String key);
}
