package com.gymapp.ui.client.main;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.audit.UserErrorMessages;
import com.gymapp.client.db.Client;
import com.gymapp.client.service.ClientService;
import com.gymapp.context.AppContext;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.ui.client.details.ClientDetailsController;
import com.gymapp.ui.client.empty.EmptyClientFormController;
import com.gymapp.ui.client.form.ClientFormController;
import com.gymapp.ui.common.DialogService;
import com.gymapp.ui.common.ViewLoader;
import com.gymapp.util.DatePickerUtils;
import com.gymapp.visit.service.VisitService;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ClientsController {

    private final ClientService clientService;
    private final MembershipRepository membershipRepository;
    private final MembershipTypeService membershipTypeService;
    private final VisitService visitService;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Client> clientsTable;

    @FXML
    private TableColumn<Client, Integer> clientNumberColumn;

    @FXML
    private TableColumn<Client, String> firstNameColumn;

    @FXML
    private TableColumn<Client, String> lastNameColumn;

    @FXML
    private TableColumn<Client, String> notesColumn;

    @FXML
    private TableColumn<Client, String> activeColumn;

    @FXML
    private TableColumn<Client, String> membershipNameColumn;

    @FXML
    private TableColumn<Client, String> membershipEndDateColumn;

    @FXML
    private TableColumn<Client, Void> visitActionColumn;

    public ClientsController() {
        this.clientService = AppContext.clientService();
        this.membershipRepository = AppContext.membershipRepository();
        this.membershipTypeService = AppContext.membershipTypeService();
        this.visitService = AppContext.visitService();
    }

    @FXML
    public void initialize() {
        initializeTable();
        initializeRowDoubleClick();
        initializeSearch();

        loadClients();
    }

    private void initializeSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchClients(newValue));
    }

    private void initializeTable() {
        clientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        clientNumberColumn.setCellValueFactory(new PropertyValueFactory<>("clientNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesColumn.setCellFactory(column -> new TableCell<>() {

            private final Label label = new Label();

            {
                label.setWrapText(true);
                label.setMaxWidth(260);
                label.setTextFill(Color.web("#111827"));
                label.getStyleClass().add("table-notes-label");

                setGraphic(label);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    label.setText("-");
                    return;
                }

                label.setText(item);
            }
        });

        activeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(resolveClientStatus(cellData.getValue()))
        );
        activeColumn.setCellFactory(column -> new ClientStatusTableCell());

        membershipNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(resolveMembershipName(cellData.getValue()))
        );
        membershipNameColumn.setCellFactory(column -> new MembershipNameTableCell());

        membershipEndDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(resolveMembershipEndDate(cellData.getValue()))
        );

        visitActionColumn.setCellFactory(column -> new VisitActionTableCell());
    }

    private void initializeRowDoubleClick() {
        clientsTable.setRowFactory(table -> {
            TableRow<Client> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openClientDetails(row.getItem());
                }
            });

            return row;
        });
    }

    @FXML
    private void onSearch() {
        searchClients(searchField.getText());
    }

    private void searchClients(String query) {
        try {
            List<Client> clients = clientService.search(query);
            setClients(clients);
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENTS_SEARCH,
                    UserErrorMessages.CLIENTS_SEARCH_FAILED,
                    "query=" + query,
                    e
            );
        }
    }

    @FXML
    private void onReset() {
        searchField.clear();
        loadClients();
    }

    @FXML
    private void onAddClient() {
        openClientForm(null, "Додати клієнта");
    }

    @FXML
    private void onEditClient() {
        Client selectedClient = getSelectedClient();

        if (selectedClient == null) {
            return;
        }

        openClientForm(selectedClient, "Редагувати клієнта");
    }

    @FXML
    private void onViewDetails() {
        openClientDetails(getSelectedClient());
    }

    @FXML
    private void onAddEmptyClient() {
        try {
            ViewLoader.showModalAndReturnController(
                    "/fxml/client/EmptyClientFormView.fxml",
                    "Додати пустий номер",
                    0.35,
                    0.42,
                    (EmptyClientFormController controller) ->
                            controller.setOnClientSaved(this::loadClients)
            );
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.EMPTY_CLIENT_FORM_OPEN,
                    UserErrorMessages.CLIENT_FORM_OPEN_FAILED,
                    e
            );
        }
    }

    @FXML
    private void onRefresh() {
        loadClients();
    }

    private void loadClients() {
        try {
            List<Client> clients = clientService.findAll();
            setClients(clients);
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENTS_LOAD,
                    UserErrorMessages.CLIENTS_LOAD_FAILED,
                    e
            );
        }
    }

    private void setClients(List<Client> clients) {
        clientsTable.setItems(FXCollections.observableArrayList(clients));
    }

    private void openClientDetails(Client client) {
        if (client == null) {
            return;
        }

        try {
            Stage stage = ViewLoader.openWindow(
                    "/fxml/client/ClientDetailsView.fxml",
                    "Деталі клієнта",
                    0.72,
                    0.78,
                    (ClientDetailsController controller) -> {
                        controller.setClient(client);
                        controller.setOnClientUpdated(this::loadClients);
                    }
            );

            stage.setMaximized(true);
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_DETAILS_OPEN,
                    UserErrorMessages.CLIENT_DETAILS_OPEN_FAILED,
                    buildClientErrorDetails(client),
                    e
            );
        }
    }

    private void openClientForm(Client client, String title) {
        try {
            ViewLoader.openWindow(
                    "/fxml/client/ClientFormView.fxml",
                    title,
                    0.72,
                    0.64,
                    (ClientFormController controller) -> {
                        if (client != null) {
                            controller.setClient(client);
                        }

                        controller.setOnClientSaved(this::loadClients);
                    }
            );
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_FORM_OPEN,
                    UserErrorMessages.CLIENT_FORM_OPEN_FAILED,
                    buildClientErrorDetails(client),
                    e
            );
        }
    }

    private String buildClientErrorDetails(Client client) {
        if (client == null) {
            return "client=null";
        }

        return "clientId=" + client.getId()
                + ", clientNumber=" + client.getClientNumber()
                + ", firstName=" + client.getFirstName()
                + ", lastName=" + client.getLastName();
    }

    private Client getSelectedClient() {
        return clientsTable.getSelectionModel().getSelectedItem();
    }

    private String resolveClientStatus(Client client) {
        if (client == null) {
            return "Неактивний";
        }

        return hasActiveMembership(client.getId())
                ? "Активний"
                : "Неактивний";
    }

    private boolean hasActiveMembership(Long clientId) {
        return membershipRepository.findActiveByClientId(clientId).isPresent();
    }

    private String resolveMembershipName(Client client) {
        if (client == null) {
            return "-";
        }

        return membershipRepository.findActiveByClientId(client.getId())
                .flatMap(membership -> membershipTypeService.findById(membership.getMembershipTypeId()))
                .map(MembershipType::getName)
                .orElse("-");
    }

    private String resolveMembershipEndDate(Client client) {
        if (client == null) {
            return "-";
        }

        return membershipRepository.findActiveByClientId(client.getId())
                .map(membership -> DatePickerUtils.format(membership.getEndDate()))
                .orElse("-");
    }

    private boolean hasVisitBasedMembership(Client client) {
        if (client == null) {
            return false;
        }

        return membershipRepository.findActiveByClientId(client.getId())
                .flatMap(membership -> membershipTypeService.findById(membership.getMembershipTypeId()))
                .map(type -> type.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS)
                .orElse(false);
    }

    private class MembershipNameTableCell extends TableCell<Client, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null || item.isBlank()) {
                setText("-");
                setStyle("");
                return;
            }

            setText(item);

            Client client = getTableView().getItems().get(getIndex());

            if (hasVisitBasedMembership(client)) {
                setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-weight: 800;");
            } else {
                setStyle("-fx-background-color: transparent; -fx-text-fill: #111827; -fx-font-weight: 400;");
            }
        }
    }

    private class VisitActionTableCell extends TableCell<Client, Void> {

        private final Button button = new Button();

        public VisitActionTableCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setPrefWidth(Double.MAX_VALUE);
            setMaxWidth(Double.MAX_VALUE);

            button.setOnAction(event -> {
                Client client = getTableView().getItems().get(getIndex());

                boolean confirmed = DialogService.showConfirm(
                        "Підтвердження",
                        "Підтвердити тренування для " + client.getFirstName() + " " + client.getLastName() + "?"
                );

                if (!confirmed) {
                    return;
                }

                try {
                    String resultMessage = visitService.registerVisit(client.getId());
                    DialogService.showInfoDialog("Реєстрація відвідування", resultMessage);
                    loadClients();
                } catch (Exception e) {
                    ErrorHandler.handle(
                            ErrorLogMessages.CLIENT_VISIT_REGISTER_FROM_TABLE,
                            UserErrorMessages.VISIT_REGISTER_FAILED,
                            buildClientErrorDetails(client),
                            e
                    );
                }
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
                return;
            }

            Client client = getTableView().getItems().get(getIndex());

            if (!hasActiveMembership(client.getId())) {

                button.setText("Немає абонемента");
                button.setDisable(true);

                button.setStyle("""
                        -fx-background-color: #f3f4f6;
                        -fx-text-fill: #6b7280;
                        -fx-font-weight: 600;
                        -fx-background-radius: 999;
                        -fx-padding: 6 12 6 12;
                        -fx-opacity: 1;
                        """);

            } else if (visitService.hasVisitToday(client.getId())) {

                button.setText("✓ Сьогодні");
                button.setDisable(true);

                button.setStyle("""
                        -fx-background-color: #dcfce7;
                        -fx-text-fill: #166534;
                        -fx-font-weight: 700;
                        -fx-background-radius: 999;
                        -fx-padding: 6 12 6 12;
                        -fx-opacity: 1;
                        """);

            } else {

                button.setText("Зареєструвати");
                button.setDisable(false);

                button.setStyle("""
                        -fx-background-color: #dbeafe;
                        -fx-text-fill: #1d4ed8;
                        -fx-font-weight: 700;
                        -fx-background-radius: 999;
                        -fx-padding: 6 12 6 12;
                        """);
            }
            setGraphic(button);
        }
    }

    private static class ClientStatusTableCell extends TableCell<Client, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setStyle("");
                return;
            }

            setText(item);

            if ("Неактивний".equals(item)) {
                setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: 700;");
            } else {
                setStyle("-fx-background-color: transparent; -fx-text-fill: #166534; -fx-font-weight: 700;");
            }
        }
    }
}