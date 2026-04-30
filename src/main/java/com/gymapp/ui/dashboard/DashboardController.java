package com.gymapp.ui.dashboard;

import com.gymapp.dashboard.dto.ClientVisitStat;
import com.gymapp.dashboard.service.DashboardAnalyticsService;
import com.gymapp.dashboard.service.DashboardService;
import com.gymapp.dashboard.dto.DashboardStats;
import com.gymapp.dashboard.dto.VisitDayStat;
import com.gymapp.membership.service.MembershipService;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.db.SqliteConnectionFactory;
import com.gymapp.client.db.SqliteClientRepository;
import com.gymapp.membership.db.SqliteMembershipRepository;
import com.gymapp.visit.db.SqliteVisitRepository;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardAnalyticsService dashboardAnalyticsService;
    private final MembershipService membershipService;

    @FXML private Label totalClientsLabel;
    @FXML private Label activeClientsLabel;
    @FXML private Label visitsTodayLabel;
    @FXML private Label expiringMembershipsLabel;

    @FXML private LineChart<String, Number> visitsByDayChart;

    @FXML private TableView<ClientVisitStat> topClientsTable;
    @FXML private TableColumn<ClientVisitStat, Number> topClientNumberColumn;
    @FXML private TableColumn<ClientVisitStat, String> topClientNameColumn;
    @FXML private TableColumn<ClientVisitStat, Number> topClientVisitsColumn;

    @FXML private TableView<ClientVisitStat> noVisitClientsTable;
    @FXML private TableColumn<ClientVisitStat, Number> noVisitClientNumberColumn;
    @FXML private TableColumn<ClientVisitStat, String> noVisitClientNameColumn;

    public DashboardController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();

        SqliteClientRepository clientRepository = new SqliteClientRepository(connectionFactory);
        SqliteVisitRepository visitRepository = new SqliteVisitRepository(connectionFactory);
        SqliteMembershipRepository membershipRepository = new SqliteMembershipRepository(connectionFactory);

        this.dashboardService = new DashboardService(
                clientRepository,
                visitRepository,
                membershipRepository
        );

        this.dashboardAnalyticsService = new DashboardAnalyticsService(connectionFactory);

        this.membershipService = new MembershipService(membershipRepository);
    }

    @FXML
    public void initialize() {
        membershipService.expireOutdatedMemberships();

        configureTables();

        loadStats();
        loadVisitsByDayChart();
        loadTopClients();
        loadActiveClientsWithoutVisits();
    }

    private void configureTables() {
        topClientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        noVisitClientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        topClientNumberColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getClientNumber() != null ? cell.getValue().getClientNumber() : 0)
        );
        topClientNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFullName())
        );
        topClientVisitsColumn.setCellValueFactory(cell ->
                new SimpleLongProperty(cell.getValue().getVisits())
        );

        noVisitClientNumberColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getClientNumber() != null ? cell.getValue().getClientNumber() : 0)
        );
        noVisitClientNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFullName())
        );
    }

    private void loadStats() {
        DashboardStats stats = dashboardService.getStats();

        totalClientsLabel.setText(String.valueOf(stats.getTotalClients()));
        activeClientsLabel.setText(String.valueOf(stats.getActiveClients()));
        visitsTodayLabel.setText(String.valueOf(stats.getVisitsToday()));
        expiringMembershipsLabel.setText(String.valueOf(stats.getExpiringMemberships()));
    }

    private void loadVisitsByDayChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
        for (VisitDayStat stat : dashboardAnalyticsService.getVisitsByDayLast30Days()) {
            series.getData().add(new XYChart.Data<>(
                    stat.getDay().format(formatter),
                    stat.getVisits()
            ));
        }

        visitsByDayChart.getData().setAll(series);
    }

    private void loadTopClients() {
        topClientsTable.setItems(
                FXCollections.observableArrayList(
                        dashboardAnalyticsService.getTopClientsByVisits(10)
                )
        );
    }

    private void loadActiveClientsWithoutVisits() {
        noVisitClientsTable.setItems(
                FXCollections.observableArrayList(
                        dashboardAnalyticsService.getActiveClientsWithoutVisits()
                )
        );
    }
}