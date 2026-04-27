package com.gymapp.ui.dashboard;

import com.gymapp.app.dashboard.DashboardService;
import com.gymapp.app.dashboard.DashboardStats;
import com.gymapp.app.membership.MembershipService;
import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.infrastructure.repository.sqlite.SqliteClientRepository;
import com.gymapp.infrastructure.repository.sqlite.SqliteMembershipRepository;
import com.gymapp.infrastructure.repository.sqlite.SqliteVisitRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    private final DashboardService dashboardService;
    private final MembershipService membershipService;

    public DashboardController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();

        this.dashboardService = new DashboardService(
                new SqliteClientRepository(connectionFactory),
                new SqliteVisitRepository(connectionFactory),
                new SqliteMembershipRepository(connectionFactory)
        );

        this.membershipService = new com.gymapp.app.membership.MembershipService(
                new SqliteMembershipRepository(connectionFactory)
        );
    }

    @FXML
    private Label totalClientsLabel;

    @FXML
    private Label activeClientsLabel;

    @FXML
    private Label visitsTodayLabel;

    @FXML
    private Label expiringMembershipsLabel;

    @FXML
    public void initialize() {
        membershipService.expireOutdatedMemberships();
        loadStats();
    }

    private void loadStats() {
        DashboardStats stats = dashboardService.getStats();

        totalClientsLabel.setText(String.valueOf(stats.getTotalClients()));
        activeClientsLabel.setText(String.valueOf(stats.getActiveClients()));
        visitsTodayLabel.setText(String.valueOf(stats.getVisitsToday()));
        expiringMembershipsLabel.setText(String.valueOf(stats.getExpiringMemberships()));
    }
}