package com.gymapp.context;

import com.gymapp.client.db.ClientRepository;
import com.gymapp.client.db.SqliteClientRepository;
import com.gymapp.client.service.ClientService;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.db.SqliteConnectionFactory;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.SqliteMembershipRepository;
import com.gymapp.membership.db.SqliteMembershipTypeRepository;
import com.gymapp.membership.service.MembershipService;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.visit.db.SqliteVisitRepository;
import com.gymapp.visit.db.VisitRepository;
import com.gymapp.visit.service.VisitService;

public class AppContext {

    private static final ConnectionFactory connectionFactory = new SqliteConnectionFactory();

    private static final ClientRepository clientRepository =
            new SqliteClientRepository(connectionFactory);

    private static final ClientService clientService =
            new ClientService(clientRepository);

    private static final MembershipRepository membershipRepository =
            new SqliteMembershipRepository(connectionFactory);

    private static final MembershipTypeService membershipTypeService =
            new MembershipTypeService(
                    new SqliteMembershipTypeRepository(connectionFactory)
            );

    private static final VisitRepository visitRepository =
            new SqliteVisitRepository(connectionFactory);

    private static final VisitService visitService =
            new VisitService(
                    visitRepository,
                    membershipRepository,
                    membershipTypeService
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

    public static MembershipTypeService membershipTypeService() {
        return membershipTypeService;
    }

    public static VisitRepository visitRepository() {
        return visitRepository;
    }

    public static VisitService visitService() {
        return visitService;
    }

    private static final MembershipService membershipService =
            new MembershipService(membershipRepository);

    public static MembershipService membershipService() {
        return membershipService;
    }
}