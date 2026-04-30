package com.gymapp.ui.client.main;

import com.gymapp.client.db.Client;
import com.gymapp.client.service.ClientService;
import com.gymapp.context.AppContext;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.ui.client.form.ClientFormController;
import com.gymapp.ui.client.details.ClientDetailsController;
import com.gymapp.ui.client.empty.EmptyClientFormController;
import com.gymapp.ui.common.ViewLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class ClientsController {

    private final ClientService clientService;
    private final MembershipRepository membershipRepository;
    private final MembershipTypeService membershipTypeService;

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
    private TableColumn<Client, String> activeColumn;

    @FXML
    private TableColumn<Client, String> membershipNameColumn;

    public ClientsController() {
        this.clientService = AppContext.clientService();
        this.membershipRepository = AppContext.membershipRepository();
        this.membershipTypeService = AppContext.membershipTypeService();
    }

    @FXML
    public void initialize() {
        initializeTable();
        initializeRowDoubleClick();

        loadClients();
    }

    private void initializeTable() {
        clientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        clientNumberColumn.setCellValueFactory(new PropertyValueFactory<>("clientNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        activeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(resolveClientStatus(cellData.getValue()))
        );
        activeColumn.setCellFactory(column -> new ClientStatusTableCell());

        membershipNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(resolveMembershipName(cellData.getValue()))
        );
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
        List<Client> clients = clientService.search(searchField.getText());
        setClients(clients);
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
    private void onDeactivateClient() {
        Client selectedClient = getSelectedClient();

        if (selectedClient == null || !selectedClient.isActive()) {
            return;
        }

        boolean confirmed = showConfirmation(
                "Підтвердження деактивації",
                "Деактивувати клієнта?",
                "Клієнт " + formatClientName(selectedClient) + " буде деактивований."
        );

        if (confirmed) {
            clientService.deactivate(selectedClient.getId());
            loadClients();
        }
    }

    @FXML
    private void onReactivateClient() {
        Client selectedClient = getSelectedClient();

        if (selectedClient == null || selectedClient.isActive()) {
            return;
        }

        boolean confirmed = showConfirmation(
                "Підтвердження реактивації",
                "Реактивувати клієнта?",
                "Клієнт " + formatClientName(selectedClient) + " буде реактивований."
        );

        if (confirmed) {
            clientService.reactivate(selectedClient.getId());
            loadClients();
        }
    }

    @FXML
    private void onAddEmptyClient() {
        ViewLoader.showModalAndReturnController(
                "/fxml/client/EmptyClientFormView.fxml",
                "Додати пустий номер",
                0.35,
                0.42,
                (EmptyClientFormController controller) ->
                        controller.setOnClientSaved(this::loadClients)
        );
    }

    private void loadClients() {
        List<Client> clients = clientService.findAll();
        setClients(clients);
    }

    private void setClients(List<Client> clients) {
        clientsTable.setItems(FXCollections.observableArrayList(clients));
    }

    private void openClientDetails(Client client) {
        if (client == null) {
            return;
        }

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
    }

    private void openClientForm(Client client, String title) {
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

    private boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert.showAndWait()
                .filter(ButtonType.OK::equals)
                .isPresent();
    }

    private String formatClientName(Client client) {
        return nullToEmpty(client.getFirstName()) + " " + nullToEmpty(client.getLastName());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
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