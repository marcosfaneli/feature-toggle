CREATE TABLE attributes (
    name VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255),
    data_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE toggles (
    name VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255),
    enabled BOOLEAN NOT NULL,
    attribute_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_toggles_attribute FOREIGN KEY (attribute_name) REFERENCES attributes (name)
);
CREATE INDEX idx_toggles_attribute_name ON toggles (attribute_name);

CREATE TABLE allow_list_entries (
    id UUID PRIMARY KEY,
    toggle_name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    CONSTRAINT fk_allow_list_toggle FOREIGN KEY (toggle_name) REFERENCES toggles (name) ON DELETE CASCADE,
    CONSTRAINT uk_allow_list_entry_toggle_value UNIQUE (toggle_name, value)
);
CREATE INDEX idx_allow_list_toggle_name ON allow_list_entries (toggle_name);

CREATE TABLE client_registrations (
    id UUID PRIMARY KEY,
    callback_url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE client_registration_toggles (
    client_registration_id UUID NOT NULL,
    toggle_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_client_toggle_registration FOREIGN KEY (client_registration_id) REFERENCES client_registrations (id) ON DELETE CASCADE,
    CONSTRAINT pk_client_registration_toggle PRIMARY KEY (client_registration_id, toggle_name)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(255) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL
);
