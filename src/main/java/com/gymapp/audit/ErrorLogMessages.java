package com.gymapp.audit;

public final class ErrorLogMessages {

    private ErrorLogMessages() {
    }

    public static final String CLIENT_FORM_SAVE = "ClientFormController.onSave";
    public static final String EMPTY_CLIENT_FORM_SAVE = "EmptyClientFormController.onSave";
    public static final String CLIENTS_LOAD = "ClientsController.loadClients";
    public static final String CLIENTS_SEARCH = "ClientsController.searchClients";
    public static final String CLIENT_DETAILS_OPEN = "ClientsController.openClientDetails";
    public static final String CLIENT_FORM_OPEN = "ClientsController.openClientForm";
    public static final String EMPTY_CLIENT_FORM_OPEN = "ClientsController.onAddEmptyClient";
    public static final String CLIENT_VISIT_REGISTER_FROM_TABLE = "ClientsController.VisitActionTableCell.registerVisit";

    public static final String CLIENT_DETAILS_MANAGE_MEMBERSHIP = "ClientDetailsController.onManageMembership";
    public static final String CLIENT_DETAILS_REGISTER_VISIT = "ClientDetailsController.onRegisterVisit";
    public static final String CLIENT_DETAILS_LOAD_VISIT_STATE = "ClientDetailsController.updateVisitedTodayIndicator";
    public static final String CLIENT_VISIT_HISTORY_OPEN = "ClientDetailsController.onViewVisitHistory";

    public static final String MEMBERSHIP_FORM_LOAD_TYPES = "ClientMembershipFormController.loadMembershipTypes";
    public static final String MEMBERSHIP_FORM_LOAD_CURRENT = "ClientMembershipFormController.loadCurrentMembership";
    public static final String MEMBERSHIP_FORM_SAVE = "ClientMembershipFormController.onSave";

    public static final String MEMBERSHIP_TYPES_LOAD = "MembershipTypesController.loadMembershipTypes";
    public static final String MEMBERSHIP_TYPE_FORM_OPEN_CREATE = "MembershipTypesController.onAddMembershipType";
    public static final String MEMBERSHIP_TYPE_FORM_OPEN_EDIT = "MembershipTypesController.onEditMembershipType";
    public static final String MEMBERSHIP_TYPE_DEACTIVATE = "MembershipTypesController.onDeactivateMembershipType";
    public static final String MEMBERSHIP_TYPE_REACTIVATE = "MembershipTypesController.onReactivateMembershipType";
    public static final String MEMBERSHIP_TYPE_FORM_SAVE = "MembershipTypeFormController.onSave";

    public static final String DATABASE_CREATE_BACKUP = "DatabaseController.onCreateBackup";
    public static final String DATABASE_SAVE_EXTRA_BACKUP_PATH = "DatabaseController.onSaveExtraBackupPath";
    public static final String DATABASE_CLEAR_EXTRA_BACKUP_PATH = "DatabaseController.onClearExtraBackupPath";
    public static final String DATABASE_RESTORE_BACKUP = "DatabaseController.onRestoreBackup";
    public static final String RESTORE_BACKUP_DIALOG_RESTORE = "RestoreBackupController.onRestore";

    public static final String MAIN_LOAD_VIEW = "MainController.loadView";
    public static final String MAIN_EXPORT_CLIENTS = "MainController.exportClients";
    public static final String MAIN_IMPORT_CLIENTS = "MainController.importClients";
    public static final String MAIN_SHOW_IMPORT_RESULT = "MainController.showImportResultDialog";
    public static final String MAIN_SHOW_INFO_DIALOG = "MainController.showInfoDialog";


    public static final String AUDIT_LOAD_ACTIVITY = "AuditLogController.loadActivityLogs";
    public static final String AUDIT_LOAD_ERRORS = "AuditLogController.loadErrorLogs";
    public static final String AUDIT_OPEN_LOG_FOLDER = "AuditLogController.openLogsFolder";

    public static final String VIEW_LOADER_LOAD_VIEW = "ViewLoader.loadView";
    public static final String APPLICATION_START = "GymApplication.start";
    public static final String APPLICATION_STARTUP_BACKUP = "GymApplication.createStartupBackupSilently";
    public static final String APPLICATION_SHUTDOWN_BACKUP = "GymApplication.createShutdownBackupSilently";

    public static final String BACKUP_CREATE_LOCAL = "BackupService.createLocalBackup";
    public static final String BACKUP_LIST = "BackupService.listBackups";
    public static final String BACKUP_RESTORE = "BackupService.restoreBackup";
    public static final String BACKUP_COPY_EXTRA = "BackupService.copyToExtraBackupPathIfConfigured";
    public static final String BACKUP_DELETE_OLD = "BackupService.deleteOldBackupsIfNeeded";
    public static final String BACKUP_SETTINGS_LOAD = "BackupSettingsService.loadProperties";
    public static final String BACKUP_SETTINGS_SAVE = "BackupSettingsService.saveProperties";
}
