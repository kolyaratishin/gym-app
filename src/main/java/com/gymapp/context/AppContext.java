package com.gymapp.context;

import com.gymapp.backup.BackupService;
import com.gymapp.backup.BackupSettingsService;
import com.gymapp.client.db.ClientRepository;
import com.gymapp.client.db.SqliteClientRepository;
import com.gymapp.client.service.ClientCsvService;
import com.gymapp.client.service.ClientService;
import com.gymapp.config.AppConfigService;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.db.SqliteConnectionFactory;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.MembershipTypeRepository;
import com.gymapp.membership.db.SqliteMembershipRepository;
import com.gymapp.membership.db.SqliteMembershipTypeRepository;
import com.gymapp.membership.service.MembershipService;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.telegram.bot.GymTelegramBot;
import com.gymapp.telegram.db.SqliteTelegramAccountRepository;
import com.gymapp.telegram.db.SqliteTelegramNotificationRepository;
import com.gymapp.telegram.db.TelegramAccountRepository;
import com.gymapp.telegram.db.TelegramNotificationRepository;
import com.gymapp.telegram.service.TelegramLinkService;
import com.gymapp.telegram.service.TelegramMessagingService;
import com.gymapp.telegram.service.TelegramNotificationService;
import com.gymapp.telegram.service.TelegramTokenValidator;
import com.gymapp.visit.db.SqliteVisitRepository;
import com.gymapp.visit.db.VisitRepository;
import com.gymapp.visit.service.VisitService;

public class AppContext {

    // DB

    private static final ConnectionFactory connectionFactory =
            new SqliteConnectionFactory();

    private static final AppConfigService appConfigService =
            new AppConfigService();

    public static AppConfigService appConfigService() {
        return appConfigService;
    }

    // Repositories

    private static final ClientRepository clientRepository =
            new SqliteClientRepository(
                    connectionFactory
            );

    private static final MembershipRepository membershipRepository =
            new SqliteMembershipRepository(
                    connectionFactory
            );

    private static final MembershipTypeRepository membershipTypeRepository =
            new SqliteMembershipTypeRepository(
                    connectionFactory
            );

    private static final VisitRepository visitRepository =
            new SqliteVisitRepository(
                    connectionFactory
            );

    private static final TelegramAccountRepository telegramAccountRepository =
            new SqliteTelegramAccountRepository(
                    connectionFactory
            );

    private static final TelegramNotificationRepository telegramNotificationRepository =
            new SqliteTelegramNotificationRepository(
                    connectionFactory
            );

    // Core services

    private static final ClientService clientService =
            new ClientService(
                    clientRepository
            );

    private static final MembershipService membershipService =
            new MembershipService(
                    membershipRepository,
                    connectionFactory
            );

    private static final MembershipTypeService membershipTypeService =
            new MembershipTypeService(
                    membershipTypeRepository
            );

    private static final ClientCsvService clientCsvService =
            new ClientCsvService(
                    clientRepository,
                    membershipRepository,
                    membershipTypeRepository
            );

    // Telegram

    private static final TelegramLinkService telegramLinkService =
            new TelegramLinkService(
                    clientService,
                    telegramAccountRepository
            );

    private static final GymTelegramBot telegramBot =
            createTelegramBot();

    private static final TelegramMessagingService telegramMessagingService =
            createTelegramMessagingService();

    private static final TelegramNotificationService telegramNotificationService =
            createTelegramNotificationService();

    private static final TelegramTokenValidator telegramTokenValidator =
            new TelegramTokenValidator();

    // Visit

    private static final VisitService visitService =
            new VisitService(
                    visitRepository,
                    membershipRepository,
                    membershipTypeService,
                    telegramNotificationService,
                    connectionFactory
            );

    // Backup

    private static final BackupSettingsService backupSettingsService =
            new BackupSettingsService();

    private static final BackupService backupService =
            new BackupService(
                    backupSettingsService
            );

    public static ClientRepository clientRepository() {
        return clientRepository;
    }

    public static ClientService clientService() {
        return clientService;
    }

    public static MembershipRepository membershipRepository() {
        return membershipRepository;
    }

    public static MembershipService membershipService() {
        return membershipService;
    }

    public static MembershipTypeService membershipTypeService() {
        return membershipTypeService;
    }

    public static VisitRepository visitRepository() {
        return visitRepository;
    }

    public static VisitService visitService() {
        return visitService;
    }

    public static BackupSettingsService backupSettingsService() {
        return backupSettingsService;
    }

    public static BackupService backupService() {
        return backupService;
    }

    public static GymTelegramBot telegramBot() {
        return telegramBot;
    }

    public static TelegramAccountRepository telegramAccountRepository() {
        return telegramAccountRepository;
    }

    public static TelegramNotificationService telegramNotificationService() {
        return telegramNotificationService;
    }

    public static TelegramLinkService telegramLinkService() {
        return telegramLinkService;
    }

    public static TelegramMessagingService telegramMessagingService() {
        return telegramMessagingService;
    }

    public static ClientCsvService clientCsvService() {
        return clientCsvService;
    }

    public static boolean isTelegramEnabled() {
        return telegramBot != null
                && telegramBot.isRunning();
    }

    public static TelegramTokenValidator telegramTokenValidator() {
        return telegramTokenValidator;
    }

    private static GymTelegramBot createTelegramBot() {
        String token =
                appConfigService.getTelegramToken();

        if (token == null) {
            return null;
        }

        return new GymTelegramBot(
                token,
                telegramLinkService
        );
    }

    private static TelegramMessagingService createTelegramMessagingService() {
        if (telegramBot == null) {
            return null;
        }

        return new TelegramMessagingService(
                telegramAccountRepository,
                clientService,
                telegramBot
        );
    }

    private static TelegramNotificationService createTelegramNotificationService() {
        if (telegramBot == null) {
            return null;
        }

        return new TelegramNotificationService(
                membershipService,
                telegramAccountRepository,
                telegramNotificationRepository,
                telegramBot
        );
    }
}