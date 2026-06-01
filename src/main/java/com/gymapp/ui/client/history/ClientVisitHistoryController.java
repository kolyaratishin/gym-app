package com.gymapp.ui.client.history;

import com.gymapp.client.db.Client;
import com.gymapp.context.AppContext;
import com.gymapp.util.DatePickerUtils;
import com.gymapp.visit.dto.ClientVisitHistoryRow;
import com.gymapp.visit.service.VisitService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientVisitHistoryController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final VisitService visitService;

    private Client client;

    @FXML
    private Label clientNameLabel;

    @FXML
    private TableView<ClientVisitHistoryRow> visitsTable;

    @FXML
    private TableColumn<ClientVisitHistoryRow, String> visitTimeColumn;

    @FXML
    private TableColumn<ClientVisitHistoryRow, String> membershipTypeColumn;

    @FXML
    private TableColumn<ClientVisitHistoryRow, String> membershipPeriodColumn;

    @FXML
    private TableColumn<ClientVisitHistoryRow, String> membershipIdColumn;

    public ClientVisitHistoryController() {
        this.visitService = AppContext.visitService();
    }

    @FXML
    public void initialize() {
        visitsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        visitTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDateTime(cellData.getValue().getVisitTime()))
        );

        membershipTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatText(cellData.getValue().getMembershipTypeName()))
        );

        membershipPeriodColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMembershipPeriod(cellData.getValue()))
        );

        membershipIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMembershipId(cellData.getValue().getMembershipId()))
        );
    }

    public void setClient(Client client) {
        this.client = client;

        clientNameLabel.setText(
                "Клієнт: " + nullToEmpty(client.getFirstName()) + " " + nullToEmpty(client.getLastName())
        );

        loadVisits();
    }

    private void loadVisits() {
        if (client == null) {
            return;
        }

        List<ClientVisitHistoryRow> rows = visitService.findHistoryByClientId(client.getId());
        visitsTable.setItems(FXCollections.observableArrayList(rows));
    }

    private String formatMembershipPeriod(ClientVisitHistoryRow row) {
        return DatePickerUtils.format(row.getMembershipStartDate())
                + " — "
                + DatePickerUtils.format(row.getMembershipEndDate());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String formatMembershipId(Long membershipId) {
        return membershipId != null ? "#" + membershipId : "-";
    }

    private String formatText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}