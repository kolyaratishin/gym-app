CREATE TABLE clients (
    id INTEGER PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    phone TEXT,
    birth_date TEXT,
    notes TEXT,
    registration_date TEXT NOT NULL,
    active INTEGER NOT NULL
);

CREATE TABLE membership_types (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    duration_days INTEGER,
    visit_limit INTEGER,
    price NUMERIC NOT NULL,
    visit_policy TEXT NOT NULL,
    active INTEGER NOT NULL
);

CREATE TABLE memberships (
    id INTEGER PRIMARY KEY,
    client_id INTEGER NOT NULL,
    membership_type_id INTEGER NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT,
    remaining_visits INTEGER,
    status TEXT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (membership_type_id) REFERENCES membership_types(id)
);

CREATE TABLE visits (
    id INTEGER PRIMARY KEY,
    client_id INTEGER NOT NULL,
    membership_id INTEGER,
    visit_time TEXT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (membership_id) REFERENCES memberships(id)
);
