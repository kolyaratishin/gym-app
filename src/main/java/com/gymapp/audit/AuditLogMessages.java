package com.gymapp.audit;

import com.gymapp.client.db.Client;
import com.gymapp.client.dto.ImportResult;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.visit.db.Visit;

import java.nio.file.Path;

public final class AuditLogMessages {

    private static final String CLIENT_DETAILS_TEMPLATE =
            "clientId=%s, clientNumber=%s, fullName='%s', phone='%s', birthDate=%s, registrationDate=%s, active=%s";

    private static final String MEMBERSHIP_TYPE_DETAILS_TEMPLATE =
            "membershipTypeId=%s, name='%s', policy=%s, durationDays=%s, visitLimit=%s, price=%s, active=%s";

    private static final String MEMBERSHIP_DETAILS_TEMPLATE =
            "membershipId=%s, clientId=%s, membershipTypeId=%s, startDate=%s, endDate=%s, remainingVisits=%s, status=%s";

    private static final String VISIT_DETAILS_TEMPLATE =
            "visitId=%s, clientId=%s, membershipId=%s, visitTime=%s, membershipTypeId=%s, membershipTypeName='%s', remainingVisitsAfterVisit=%s, membershipStatusAfterVisit=%s";

    private static final String CLIENT_CREATED_TEMPLATE = "Створено клієнта: %s";
    private static final String EMPTY_CLIENT_CREATED_TEMPLATE = "Створено порожню картку клієнта: %s";
    private static final String CLIENT_UPDATED_TEMPLATE = "Оновлено клієнта: %s";

    private static final String MEMBERSHIP_CREATED_TEMPLATE = "Створено абонемент: %s. Тип: %s";
    private static final String MEMBERSHIP_DEACTIVATED_TEMPLATE = "Деактивовано попередній активний абонемент перед заміною: %s";
    private static final String MEMBERSHIP_EXPIRED_TEMPLATE = "Абонемент автоматично переведено в статус EXPIRED під час реєстрації відвідування: %s. Причина: %s";

    private static final String MEMBERSHIP_TYPE_CREATED_TEMPLATE = "Створено тип абонемента: %s";
    private static final String MEMBERSHIP_TYPE_UPDATED_TEMPLATE = "Оновлено тип абонемента: %s";
    private static final String MEMBERSHIP_TYPE_DEACTIVATED_TEMPLATE = "Деактивовано тип абонемента: membershipTypeId=%s";
    private static final String MEMBERSHIP_TYPE_REACTIVATED_TEMPLATE = "Активовано тип абонемента: membershipTypeId=%s";

    private static final String VISIT_REGISTERED_TEMPLATE = "Зареєстровано відвідування: %s";

    private static final String CLIENTS_EXPORTED_TEMPLATE = "Експортовано клієнтів у CSV: file='%s', clientsCount=%s";
    private static final String CLIENTS_IMPORTED_TEMPLATE = "Імпортовано клієнтів з CSV: file='%s', imported=%s, membershipsImported=%s, skipped=%s, errorsCount=%s";

    private static final String BACKUP_CREATED_TEMPLATE = "Створено резервну копію БД: dbPath='%s', backupFile='%s', extraBackupPath='%s'";
    private static final String BACKUP_RESTORED_TEMPLATE = "Відновлено БД з резервної копії: backupFile='%s', dbPath='%s'";
    private static final String BACKUP_EXTRA_PATH_UPDATED_TEMPLATE = "Оновлено додатковий шлях для backup: path='%s'";
    private static final String BACKUP_EXTRA_PATH_CLEARED_TEMPLATE = "Очищено додатковий шлях для backup";

    private AuditLogMessages() {
    }

    public static String clientCreated(Client client) {
        return CLIENT_CREATED_TEMPLATE.formatted(clientDetails(client));
    }

    public static String emptyClientCreated(Client client) {
        return EMPTY_CLIENT_CREATED_TEMPLATE.formatted(clientDetails(client));
    }

    public static String clientUpdated(Client client) {
        return CLIENT_UPDATED_TEMPLATE.formatted(clientDetails(client));
    }

    public static String membershipCreated(Membership membership, MembershipType membershipType) {
        return MEMBERSHIP_CREATED_TEMPLATE.formatted(
                membershipDetails(membership),
                membershipTypeDetails(membershipType)
        );
    }

    public static String membershipDeactivated(Membership membership) {
        return MEMBERSHIP_DEACTIVATED_TEMPLATE.formatted(membershipDetails(membership));
    }

    public static String membershipExpired(Membership membership, String reason) {
        return MEMBERSHIP_EXPIRED_TEMPLATE.formatted(membershipDetails(membership), reason);
    }

    public static String membershipTypeCreated(MembershipType membershipType) {
        return MEMBERSHIP_TYPE_CREATED_TEMPLATE.formatted(membershipTypeDetails(membershipType));
    }

    public static String membershipTypeUpdated(MembershipType membershipType) {
        return MEMBERSHIP_TYPE_UPDATED_TEMPLATE.formatted(membershipTypeDetails(membershipType));
    }

    public static String membershipTypeDeactivated(Long id) {
        return MEMBERSHIP_TYPE_DEACTIVATED_TEMPLATE.formatted(id);
    }

    public static String membershipTypeReactivated(Long id) {
        return MEMBERSHIP_TYPE_REACTIVATED_TEMPLATE.formatted(id);
    }

    public static String visitRegistered(Visit visit, Membership membership, MembershipType membershipType) {
        return VISIT_REGISTERED_TEMPLATE.formatted(
                VISIT_DETAILS_TEMPLATE.formatted(
                        visit.getId(),
                        visit.getClientId(),
                        visit.getMembershipId(),
                        visit.getVisitTime(),
                        membershipType.getId(),
                        membershipType.getName(),
                        membership.getRemainingVisits(),
                        membership.getStatus()
                )
        );
    }

    public static String clientsExported(Path outputFile, int clientsCount) {
        return CLIENTS_EXPORTED_TEMPLATE.formatted(outputFile, clientsCount);
    }

    public static String clientsImported(Path inputFile, ImportResult result) {
        return CLIENTS_IMPORTED_TEMPLATE.formatted(
                inputFile,
                result.getImported(),
                result.getMembershipsImported(),
                result.getSkipped(),
                result.getErrors().size()
        );
    }

    public static String backupCreated(Path dbPath, Path backupFile, Path extraBackupPath) {
        return BACKUP_CREATED_TEMPLATE.formatted(dbPath, backupFile, extraBackupPath);
    }

    public static String backupRestored(Path backupFile, Path dbPath) {
        return BACKUP_RESTORED_TEMPLATE.formatted(backupFile, dbPath);
    }

    public static String backupExtraPathUpdated(String path) {
        return BACKUP_EXTRA_PATH_UPDATED_TEMPLATE.formatted(path);
    }

    public static String backupExtraPathCleared() {
        return BACKUP_EXTRA_PATH_CLEARED_TEMPLATE;
    }

    private static String clientDetails(Client client) {
        return CLIENT_DETAILS_TEMPLATE.formatted(
                client.getId(),
                client.getClientNumber(),
                client.getFullName(),
                client.getPhone(),
                client.getBirthDate(),
                client.getRegistrationDate(),
                client.isActive()
        );
    }

    private static String membershipTypeDetails(MembershipType membershipType) {
        return MEMBERSHIP_TYPE_DETAILS_TEMPLATE.formatted(
                membershipType.getId(),
                membershipType.getName(),
                membershipType.getVisitPolicy(),
                membershipType.getDurationDays(),
                membershipType.getVisitLimit(),
                membershipType.getPrice(),
                membershipType.isActive()
        );
    }

    private static String membershipDetails(Membership membership) {
        return MEMBERSHIP_DETAILS_TEMPLATE.formatted(
                membership.getId(),
                membership.getClientId(),
                membership.getMembershipTypeId(),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getRemainingVisits(),
                membership.getStatus()
        );
    }
}
