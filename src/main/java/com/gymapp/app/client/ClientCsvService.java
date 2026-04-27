package com.gymapp.app.client;

import com.gymapp.domain.client.Client;
import com.gymapp.domain.membership.Membership;
import com.gymapp.domain.membership.MembershipStatus;
import com.gymapp.domain.membership.MembershipType;
import com.gymapp.domain.repository.ClientRepository;
import com.gymapp.domain.repository.MembershipRepository;
import com.gymapp.domain.repository.MembershipTypeRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientCsvService {

    private static final String HEADER =
            "first_name,last_name,phone,birth_date,notes,registration_date,membership_type,start_date,end_date,remaining_visits";

    private final ClientRepository clientRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipTypeRepository membershipTypeRepository;

    public ClientCsvService(
            ClientRepository clientRepository,
            MembershipRepository membershipRepository,
            MembershipTypeRepository membershipTypeRepository
    ) {
        this.clientRepository = clientRepository;
        this.membershipRepository = membershipRepository;
        this.membershipTypeRepository = membershipTypeRepository;
    }

    public Path exportClients(Path outputFile) {
        List<Client> clients = clientRepository.findAll();

        try {
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();

                for (Client client : clients) {
                    writer.write(toCsvRow(client));
                    writer.newLine();
                }
            }

            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to export clients to CSV", e);
        }
    }

    public ImportResult importClients(Path inputFile) {
        int imported = 0;
        int membershipsImported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(inputFile, StandardCharsets.UTF_8);

            if (lines.isEmpty()) {
                return new ImportResult(0, 0, 0, List.of("CSV файл порожній"));
            }

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line == null || line.isBlank()) {
                    skipped++;
                    continue;
                }

                try {
                    ImportRow row = parseImportRow(line);

                    Client savedClient = clientRepository.save(row.client());
                    imported++;

                    if (row.hasMembershipData()) {
                        try {
                            createMembershipForClient(savedClient.getId(), row);
                            membershipsImported++;
                        } catch (Exception e) {
                            errors.add("Рядок " + (i + 1) + ": клієнта імпортовано, але абонемент не створено — " + e.getMessage());
                        }
                    }

                } catch (Exception e) {
                    skipped++;
                    errors.add("Рядок " + (i + 1) + ": " + e.getMessage());
                }
            }

            return new ImportResult(imported, membershipsImported, skipped, errors);
        } catch (IOException e) {
            throw new RuntimeException("Failed to import clients from CSV", e);
        }
    }

    private ImportRow parseImportRow(String line) {
        List<String> values = parseCsvLine(line);

        if (values.size() < 6) {
            throw new RuntimeException("Очікується мінімум 6 колонок");
        }

        String firstName = get(values, 0).trim();
        String lastName = get(values, 1).trim();

        if (firstName.isBlank()) {
            throw new RuntimeException("Ім'я є обов'язковим");
        }

        if (lastName.isBlank()) {
            throw new RuntimeException("Прізвище є обов'язковим");
        }

        Client client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setPhone(emptyToNull(get(values, 2)));
        client.setBirthDate(parseDateOrNull(get(values, 3), "birth_date"));
        client.setNotes(emptyToNull(get(values, 4)));

        LocalDate registrationDate = parseDateOrNull(get(values, 5), "registration_date");
        client.setRegistrationDate(registrationDate != null ? registrationDate : LocalDate.now());

        client.setActive(false);

        String membershipTypeName = emptyToNull(get(values, 6));
        LocalDate startDate = parseDateOrNull(get(values, 7), "start_date");
        LocalDate endDate = parseDateOrNull(get(values, 8), "end_date");
        Integer remainingVisits = parseIntegerOrNull(get(values, 9), "remaining_visits");

        return new ImportRow(
                client,
                membershipTypeName,
                startDate,
                endDate,
                remainingVisits
        );
    }

    private void createMembershipForClient(Long clientId, ImportRow row) {
        if (row.membershipTypeName() == null || row.membershipTypeName().isBlank()) {
            return;
        }

        Optional<MembershipType> membershipTypeOptional =
                membershipTypeRepository.findByName(row.membershipTypeName());

        if (membershipTypeOptional.isEmpty()) {
            throw new RuntimeException("не знайдено тип абонемента \"" + row.membershipTypeName() + "\"");
        }

        MembershipType membershipType = membershipTypeOptional.get();

        LocalDate startDate = row.startDate() != null ? row.startDate() : LocalDate.now();
        LocalDate endDate = row.endDate();

        Integer remainingVisits = row.remainingVisits();

        if (membershipType.getVisitPolicy() == com.gymapp.domain.membership.VisitPolicy.LIMITED_BY_VISITS
                && remainingVisits == null) {
            throw new RuntimeException("для абонемента по кількості потрібно вказати remaining_visits");
        }

        if (endDate == null && membershipType.getDurationDays() != null) {
            endDate = startDate.plusDays(membershipType.getDurationDays());
        }

        Membership membership = new Membership();
        membership.setClientId(clientId);
        membership.setMembershipTypeId(membershipType.getId());
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);

        if (membershipType.getVisitPolicy() == com.gymapp.domain.membership.VisitPolicy.LIMITED_BY_VISITS) {
            membership.setRemainingVisits(remainingVisits);
        } else {
            membership.setRemainingVisits(null);
        }

        membership.setStatus(resolveMembershipStatus(endDate, remainingVisits));

        membershipRepository.save(membership);
    }

    private MembershipStatus resolveMembershipStatus(LocalDate endDate, Integer remainingVisits) {
        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            return MembershipStatus.EXPIRED;
        }

        if (remainingVisits != null && remainingVisits <= 0) {
            return MembershipStatus.EXPIRED;
        }

        return MembershipStatus.ACTIVE;
    }

    private String toCsvRow(Client client) {
        return String.join(",",
                escape(client.getFirstName()),
                escape(client.getLastName()),
                escape(client.getPhone()),
                escape(client.getBirthDate() != null ? client.getBirthDate().toString() : null),
                escape(client.getNotes()),
                escape(client.getRegistrationDate() != null ? client.getRegistrationDate().toString() : null),
                "",
                "",
                "",
                ""
        );
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result;
    }

    private String get(List<String> values, int index) {
        if (index >= values.size()) {
            return "";
        }
        return values.get(index);
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private LocalDate parseDateOrNull(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            throw new RuntimeException("некоректна дата в полі " + fieldName + ": " + value);
        }
    }

    private Integer parseIntegerOrNull(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new RuntimeException("некоректне число в полі " + fieldName + ": " + value);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private record ImportRow(
            Client client,
            String membershipTypeName,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        boolean hasMembershipData() {
            return membershipTypeName != null && !membershipTypeName.isBlank();
        }
    }
}