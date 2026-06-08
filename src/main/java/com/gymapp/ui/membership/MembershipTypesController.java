package com.gymapp.ui.membership;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.audit.UserErrorMessages;
import com.gymapp.context.AppContext;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.util.GymAppUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;
import javafx.stage.Stage;

public class MembershipTypesController {

    private final MembershipTypeService membershipTypeService;

    public MembershipTypesController() {
        this.membershipTypeService = AppContext.membershipTypeService();
    }

    @FXML
    private TableView<MembershipType> membershipTypesTable;

    @FXML
    private TableColumn<MembershipType, Long> idColumn;

    @FXML
    private TableColumn<MembershipType, String> nameColumn;

    @FXML
    private TableColumn<MembershipType, Integer> durationDaysColumn;

    @FXML
    private TableColumn<MembershipType, Integer> visitLimitColumn;

    @FXML
    private TableColumn<MembershipType, BigDecimal> priceColumn;

    @FXML
    private TableColumn<MembershipType, Boolean> activeColumn;

    @FXML
    public void initialize() {
        membershipTypesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        durationDaysColumn.setCellValueFactory(new PropertyValueFactory<>("durationDays"));
        visitLimitColumn.setCellValueFactory(new PropertyValueFactory<>("visitLimit"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        loadMembershipTypes();
    }

    @FXML
    private void onAddMembershipType() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/membership/MembershipTypeFormView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            MembershipTypeFormController controller = loader.getController();
            controller.setOnMembershipTypeSaved(this::loadMembershipTypes);

            Stage stage = new Stage();
            GymAppUtils.applyResponsiveStageSize(stage, 0.72, 0.78);
            stage.setTitle("Додати тип абонемента");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.MEMBERSHIP_TYPE_FORM_OPEN_CREATE,
                    UserErrorMessages.MEMBERSHIP_TYPE_FORM_OPEN_FAILED,
                    e
            );
        }
    }

    @FXML
    private void onEditMembershipType() {
        MembershipType selected = membershipTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/membership/MembershipTypeFormView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            MembershipTypeFormController controller = loader.getController();
            controller.setMembershipType(selected);
            controller.setOnMembershipTypeSaved(this::loadMembershipTypes);

            Stage stage = new Stage();
            GymAppUtils.applyResponsiveStageSize(stage, 0.72, 0.78);
            stage.setTitle("Редагувати тип абонемента");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.MEMBERSHIP_TYPE_FORM_OPEN_EDIT,
                    UserErrorMessages.MEMBERSHIP_TYPE_FORM_OPEN_FAILED,
                    "membershipTypeId=" + selected.getId() + ", name=" + selected.getName(),
                    e
            );
        }
    }

    @FXML
    private void onViewDetails() {
        System.out.println("View details");
    }

    @FXML
    private void onDeactivateMembershipType() {
        MembershipType selected = membershipTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isActive()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження деактивації");
        alert.setHeaderText("Деактивувати тип абонемента?");
        alert.setContentText("Тип абонемента \"" + selected.getName() + "\" буде деактивований.");

        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                membershipTypeService.deactivate(selected.getId());
                loadMembershipTypes();
            } catch (Exception e) {
                ErrorHandler.handle(
                        ErrorLogMessages.MEMBERSHIP_TYPE_DEACTIVATE,
                        UserErrorMessages.MEMBERSHIP_TYPE_DEACTIVATE_FAILED,
                        "membershipTypeId=" + selected.getId() + ", name=" + selected.getName(),
                        e
                );
            }
        }
    }

    @FXML
    private void onReactivateMembershipType() {
        MembershipType selected = membershipTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isActive()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження реактивації");
        alert.setHeaderText("Реактивувати тип абонемента?");
        alert.setContentText("Тип абонемента \"" + selected.getName() + "\" буде реактивований.");

        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                membershipTypeService.reactivate(selected.getId());
                loadMembershipTypes();
            } catch (Exception e) {
                ErrorHandler.handle(
                        ErrorLogMessages.MEMBERSHIP_TYPE_REACTIVATE,
                        UserErrorMessages.MEMBERSHIP_TYPE_REACTIVATE_FAILED,
                        "membershipTypeId=" + selected.getId() + ", name=" + selected.getName(),
                        e
                );
            }
        }
    }

    private void loadMembershipTypes() {
        try {
            List<MembershipType> membershipTypes = membershipTypeService.findAll();
            membershipTypesTable.setItems(FXCollections.observableArrayList(membershipTypes));
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.MEMBERSHIP_TYPES_LOAD,
                    UserErrorMessages.MEMBERSHIP_TYPES_LOAD_FAILED,
                    e
            );
        }
    }
}