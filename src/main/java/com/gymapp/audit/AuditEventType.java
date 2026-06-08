package com.gymapp.audit;

public enum AuditEventType {

    CLIENT_CREATED("Створено клієнта"),
    CLIENT_UPDATED("Оновлено клієнта"),
    CLIENT_DELETED("Видалено клієнта"),

    MEMBERSHIP_CREATED("Створено абонемент"),
    MEMBERSHIP_UPDATED("Оновлено абонемент"),
    MEMBERSHIP_DEACTIVATED("Деактивовано абонемент"),

    VISIT_REGISTERED("Зареєстровано відвідування"),

    BACKUP_CREATED("Створено резервну копію"),
    BACKUP_RESTORED("Відновлено базу"),

    TELEGRAM_MESSAGE_SENT("Надіслано Telegram повідомлення");

    private final String label;

    AuditEventType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}