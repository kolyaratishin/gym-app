package com.gymapp.audit;

public final class UserErrorMessages {

    private UserErrorMessages() {
    }

    public static final String CLIENT_SAVE_FAILED = "Не вдалося зберегти клієнта.";
    public static final String EMPTY_CLIENT_SAVE_FAILED = "Не вдалося створити пустий номер клієнта.";
    public static final String CLIENTS_LOAD_FAILED = "Не вдалося завантажити список клієнтів.";
    public static final String CLIENTS_SEARCH_FAILED = "Не вдалося виконати пошук клієнтів.";
    public static final String CLIENT_DETAILS_OPEN_FAILED = "Не вдалося відкрити деталі клієнта.";
    public static final String CLIENT_FORM_OPEN_FAILED = "Не вдалося відкрити форму клієнта.";
    public static final String VISIT_REGISTER_FAILED = "Не вдалося зареєструвати відвідування.";
    public static final String VISIT_STATE_LOAD_FAILED = "Не вдалося оновити статус відвідування за сьогодні.";
    public static final String VISIT_HISTORY_OPEN_FAILED = "Не вдалося відкрити історію відвідувань.";

    public static final String MEMBERSHIP_FORM_LOAD_FAILED = "Не вдалося завантажити дані для форми абонемента.";
    public static final String MEMBERSHIP_SAVE_FAILED = "Не вдалося зберегти абонемент.";
    public static final String MEMBERSHIP_MANAGE_OPEN_FAILED = "Не вдалося відкрити керування абонементом.";

    public static final String MEMBERSHIP_TYPES_LOAD_FAILED = "Не вдалося завантажити типи абонементів.";
    public static final String MEMBERSHIP_TYPE_FORM_OPEN_FAILED = "Не вдалося відкрити форму типу абонемента.";
    public static final String MEMBERSHIP_TYPE_SAVE_FAILED = "Не вдалося зберегти тип абонемента.";
    public static final String MEMBERSHIP_TYPE_DEACTIVATE_FAILED = "Не вдалося деактивувати тип абонемента.";
    public static final String MEMBERSHIP_TYPE_REACTIVATE_FAILED = "Не вдалося реактивувати тип абонемента.";

    public static final String BACKUP_CREATE_FAILED = "Не вдалося створити резервну копію.";
    public static final String BACKUP_RESTORE_FAILED = "Не вдалося відновити базу з резервної копії.";
    public static final String BACKUP_SETTINGS_SAVE_FAILED = "Не вдалося зберегти налаштування резервної копії.";
    public static final String BACKUP_SETTINGS_CLEAR_FAILED = "Не вдалося очистити додатковий шлях для резервної копії.";

    public static final String VIEW_LOAD_FAILED = "Не вдалося відкрити сторінку.";
    public static final String EXPORT_CLIENTS_FAILED = "Не вдалося експортувати клієнтів.";
    public static final String IMPORT_CLIENTS_FAILED = "Не вдалося імпортувати клієнтів.";
    public static final String APPLICATION_START_FAILED = "Не вдалося запустити додаток.";

    public static final String DETAILS_IN_ERROR_LOG = "\n\nДеталі записані у журнал помилок.";
}
