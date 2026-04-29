package com.gymapp.ui.client;

import com.gymapp.client.service.ClientService;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.client.db.Client;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.client.db.SqliteClientRepository;
import com.gymapp.infrastructure.util.GymAppUtils;
import java.util.List;

import com.gymapp.membership.db.SqliteMembershipRepository;
import com.gymapp.membership.db.SqliteMembershipTypeRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ClientsController {

    private final ClientService clientService;
    private final MembershipRepository membershipRepository;
    private final MembershipTypeService membershipTypeService;

    public ClientsController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();
        SqliteClientRepository clientRepository = new SqliteClientRepository(connectionFactory);
        this.clientService = new ClientService(clientRepository);
        this.membershipRepository = new SqliteMembershipRepository(connectionFactory);
        this.membershipTypeService = new MembershipTypeService(
                new SqliteMembershipTypeRepository(connectionFactory)
        );
    }

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

    @FXML
    public void initialize() {
        clientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        clientNumberColumn.setCellValueFactory(new PropertyValueFactory<>("clientNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        activeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        hasActiveMembership(cellData.getValue().getId())
                                ? "Активний"
                                : "Неактивний"
                )
        );

        activeColumn.setCellFactory(column -> new TableCell<>() {
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
        });

        membershipNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue() != null
                                ? resolveMembershipName(cellData.getValue().getId())
                                : "-"
                )
        );

        loadClients();
        clientsTable.setRowFactory(table -> {
            TableRow<Client> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Client client = row.getItem();
                    openClientDetails(client);
                }
            });

            return row;
        });
    }

    private void openClientDetails(Client client) {
        System.out.println(client);
        if (client == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/client/ClientDetailsView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            ClientDetailsController controller = loader.getController();
            controller.setClient(client);
            controller.setOnClientUpdated(this::loadClients);

            Stage stage = new Stage();
            GymAppUtils.applyResponsiveStageSize(stage, 0.72, 0.78);
            stage.setMaximized(true);
            stage.setTitle("Деталі клієнта");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open client details view", e);
        }
    }

    private boolean hasActiveMembership(Long clientId) {
        return membershipRepository.findActiveByClientId(clientId).isPresent();
    }

    private String resolveMembershipName(Long clientId) {
        try {
            return membershipRepository.findActiveByClientId(clientId)
                    .flatMap(membership -> membershipTypeService.findById(membership.getMembershipTypeId()))
                    .map(MembershipType::getName)
                    .orElse("-");
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }

    @FXML
    private void onSearch() {
        List<Client> clients = clientService.search(searchField.getText());
        clientsTable.setItems(FXCollections.observableArrayList(clients));
    }

    @FXML
    private void onReset() {
        searchField.clear();
        loadClients();
    }

    @FXML
    private void onAddClient() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/client/ClientFormView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            ClientFormController controller = loader.getController();
            controller.setOnClientSaved(this::loadClients);

            Stage stage = new Stage();
            GymAppUtils.applyResponsiveStageSize(stage, 0.72, 0.78);
            stage.setTitle("Додати клієнта");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open client form", e);
        }
    }

    @FXML
    private void onEditClient() {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/client/ClientFormView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            ClientFormController controller = loader.getController();
            controller.setClient(selectedClient);
            controller.setOnClientSaved(this::loadClients);

            Stage stage = new Stage();
            GymAppUtils.applyResponsiveStageSize(stage, 0.72, 0.78);
            stage.setTitle("Редагувати клієнта");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open edit client form", e);
        }
    }

    @FXML
    private void onViewDetails() {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        openClientDetails(selectedClient);
    }

    @FXML
    private void onDeactivateClient() {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            return;
        }

        if (!selectedClient.isActive()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження деактивації");
        alert.setHeaderText("Деактивувати клієнта?");
        alert.setContentText("Клієнт " + selectedClient.getFirstName() + " " + selectedClient.getLastName() + " буде деактивований.");

        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            clientService.deactivate(selectedClient.getId());
            loadClients();
        }
    }

    @FXML
    private void onReactivateClient() {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            return;
        }

        if (selectedClient.isActive()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження реактивації");
        alert.setHeaderText("Реактивувати клієнта?");
        alert.setContentText("Клієнт " + selectedClient.getFirstName() + " " + selectedClient.getLastName() + " буде реактивований.");

        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            clientService.reactivate(selectedClient.getId());
            loadClients();
        }
    }

    private void loadClients() {
        try {
            List<Client> clients = clientService.findAll();
            System.out.println("Loaded clients count: " + clients.size());
            clientsTable.setItems(FXCollections.observableArrayList(clients));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load clients", e);
        }
    }

    @FXML
    private void onAddEmptyClient() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/client/EmptyClientFormView.fxml")
            );

            Scene scene = new Scene(loader.load(), 520, 320);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            EmptyClientFormController controller = loader.getController();
            controller.setOnClientSaved(this::loadClients);

            Stage stage = new Stage();
            stage.setTitle("Додати пустий номер");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open empty client form", e);
        }
    }
}