package com.gymapp.audit;

public enum AuditEventType {

    CLIENT_CREATED("Створено клієнта"),
    CLIENT_UPDATED("Оновлено клієнта"),
    CLIENT_DELETED("Видалено клієнта"),

    MEMBERSHIP_CREATED("Створено абонемент"),
    MEMBERSHIP_UPDATED("Оновлено абонемент"),
    MEMBERSHIP_DEACTIVATED("Деактивовано абонемент"),
    MEMBERSHIP_EXPIRED("Абонемент став неактивним"),

    MEMBERSHIP_TYPE_CREATED("Створено тип абонемента"),
    MEMBERSHIP_TYPE_UPDATED("Оновлено тип абонемента"),
    MEMBERSHIP_TYPE_DEACTIVATED("Деактивовано тип абонемента"),
    MEMBERSHIP_TYPE_REACTIVATED("Активовано тип абонемента"),

    VISIT_REGISTERED("Зареєстровано відвідування"),

    CLIENTS_IMPORTED("Імпортовано клієнтів"),
    CLIENTS_EXPORTED("Експортовано клієнтів"),

    BACKUP_CREATED("Створено резервну копію"),
    BACKUP_RESTORED("Відновлено базу"),
    BACKUP_EXTRA_PATH_UPDATED("Оновлено шлях додаткової резервної копії"),
    BACKUP_EXTRA_PATH_CLEARED("Очищено шлях додаткової резервної копії"),

    TELEGRAM_MESSAGE_SENT("Надіслано Telegram повідомлення");

    private final String label;

    AuditEventType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
