DROP TABLE IF EXISTS borrowing_record;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS account;

CREATE TABLE account (
    user_id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    user_name TEXT UNIQUE NOT NULL,
    registration_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMPTZ
);

CREATE TABLE book (
    isbn VARCHAR(20) PRIMARY KEY,
    name TEXT NOT NULL,
    author TEXT,
    introduction TEXT
);

CREATE TABLE inventory (
    inventory_id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL REFERENCES book(isbn) ON DELETE RESTRICT,
    store_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_inventory_status CHECK (status IN ('AVAILABLE', 'BORROWED', 'DISCARD'))
);

CREATE TABLE borrowing_record (
    borrowing_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES account(user_id) ON DELETE RESTRICT,
    inventory_id BIGINT NOT NULL REFERENCES inventory(inventory_id) ON DELETE RESTRICT,
    borrowing_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    return_time TIMESTAMPTZ
);

CREATE INDEX idx_borrowing_record_user_id ON borrowing_record(user_id);
CREATE INDEX idx_borrowing_record_inventory_id ON borrowing_record(inventory_id);
