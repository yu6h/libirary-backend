DROP TABLE IF EXISTS borrowing_record^
DROP TABLE IF EXISTS inventory^
DROP TABLE IF EXISTS book^
DROP TABLE IF EXISTS account^

CREATE TABLE account (
    user_id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    user_name TEXT UNIQUE NOT NULL,
    registration_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMPTZ
)^

CREATE TABLE book (
    isbn VARCHAR(20) PRIMARY KEY,
    name TEXT NOT NULL,
    author TEXT,
    introduction TEXT
)^

CREATE TABLE inventory (
    inventory_id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL REFERENCES book(isbn) ON DELETE RESTRICT,
    store_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_inventory_status CHECK (status IN ('AVAILABLE', 'BORROWED', 'DISCARD'))
)^

CREATE TABLE borrowing_record (
    borrowing_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES account(user_id) ON DELETE RESTRICT,
    inventory_id BIGINT NOT NULL REFERENCES inventory(inventory_id) ON DELETE RESTRICT,
    borrowing_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    return_time TIMESTAMPTZ
)^

CREATE INDEX idx_borrowing_record_user_id ON borrowing_record(user_id)^
CREATE INDEX idx_borrowing_record_inventory_id ON borrowing_record(inventory_id)^

-- 借閱流程改由 DB 端 stored procedure（實作為 PostgreSQL function）完成：
-- 同一交易中檢查重複借閱、悲觀鎖定一筆可用庫存、更新為 BORROWED、寫入 borrowing_record。
-- 語句結尾使用 ^（見 application.properties spring.sql.init.separator），避免 Spring 以 ; 切開 PL/pgSQL 內文。
CREATE OR REPLACE FUNCTION sp_borrow_book(
    p_user_id BIGINT,
    p_isbn VARCHAR(20),
    OUT result_status VARCHAR(32),
    OUT borrowing_id BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_inventory_id BIGINT;
BEGIN
    borrowing_id := NULL;

    IF EXISTS (
        SELECT 1
        FROM borrowing_record br
        INNER JOIN inventory i ON i.inventory_id = br.inventory_id
        WHERE br.user_id = p_user_id
          AND i.isbn = p_isbn
          AND br.return_time IS NULL
    ) THEN
        result_status := 'DUPLICATE';
        RETURN;
    END IF;

    SELECT i.inventory_id INTO v_inventory_id
    FROM inventory i
    WHERE i.isbn = p_isbn
      AND i.status = 'AVAILABLE'
    ORDER BY i.inventory_id
    FOR UPDATE
    LIMIT 1;

    IF v_inventory_id IS NULL THEN
        result_status := 'NO_STOCK';
        RETURN;
    END IF;

    UPDATE inventory
    SET status = 'BORROWED'
    WHERE inventory_id = v_inventory_id;

    INSERT INTO borrowing_record (user_id, inventory_id, borrowing_time, return_time)
    VALUES (p_user_id, v_inventory_id, CURRENT_TIMESTAMP, NULL)
    RETURNING borrowing_record.borrowing_id INTO borrowing_id;

    result_status := 'OK';
END;
$$^
