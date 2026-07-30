-- V1__init.sql
--
-- Single entry-point migration: full baseline schema for every entity, plus the seed ROLE_ADMIN
-- account needed for the very first login.

-- ---------------------------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NULL,
    username    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    is_enable   BIT(1)       NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_user_id  UNIQUE (user_id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

-- ---------------------------------------------------------------------------------------------
-- category
-- ---------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS category (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_id  VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NULL,
    description  VARCHAR(500) NULL,
    image_key    VARCHAR(500) NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_category_category_id UNIQUE (category_id),
    CONSTRAINT uq_category_name        UNIQUE (name)
);

-- ---------------------------------------------------------------------------------------------
-- product
-- ---------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product (
    id           BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id   VARCHAR(255)   NOT NULL,
    name         VARCHAR(255)   NULL,
    price        DECIMAL(19,2)  NULL,
    tax_rate     DECIMAL(19,2)  NULL,
    description  VARCHAR(500)   NULL,
    image_key    VARCHAR(500)   NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    category_id  BIGINT         NOT NULL,
    CONSTRAINT uq_product_product_id UNIQUE (product_id),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES category (id)
        ON DELETE RESTRICT
) ;

CREATE INDEX idx_product_category_id ON product (category_id);

-- ---------------------------------------------------------------------------------------------
-- inventory
-- ---------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    inventory_id        VARCHAR(255) NOT NULL,
    product_id          BIGINT       NULL,
    available_quantity  INT          NOT NULL,
    low_stock_threshold INT          NOT NULL,
    active              BIT(1)       NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version             BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventory_inventory_id UNIQUE (inventory_id),
    CONSTRAINT uq_inventory_product_id   UNIQUE (product_id),
    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id) REFERENCES product (id)
) ;

-- ---------------------------------------------------------------------------------------------
-- invoice
-- ---------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    invoice_id       VARCHAR(255)  NOT NULL,
    user_id          BIGINT        NULL,
    customer_name    VARCHAR(255)  NULL,
    customer_number  VARCHAR(255)  NULL,
    customer_email   VARCHAR(255)  NULL,
    sub_total        DECIMAL(19,2) NULL,
    tax              DECIMAL(19,2) NULL,
    grand_total      DECIMAL(19,2) NULL,
    invoice_status   VARCHAR(20)   NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version          BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_invoice_invoice_id UNIQUE (invoice_id),
    CONSTRAINT fk_invoice_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ;

CREATE INDEX idx_invoice_user_id ON invoice (user_id);

-- ---------------------------------------------------------------------------------------------
-- invoice_item
-- ---------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_item (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    invoice_id       BIGINT        NULL,
    invoice_item_id  VARCHAR(255)  NOT NULL,
    product_id       BIGINT        NULL,
    quantity         INT           NULL,
    unit_price       DECIMAL(19,2) NULL,
    line_total       DECIMAL(19,2) NULL,
    CONSTRAINT uq_invoice_item_invoice_item_id UNIQUE (invoice_item_id),
    CONSTRAINT fk_invoice_item_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoice (id),
    CONSTRAINT fk_invoice_item_product
        FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE INDEX idx_invoice_item_invoice_id ON invoice_item (invoice_id);
CREATE INDEX idx_invoice_item_product_id ON invoice_item (product_id);

-- ---------------------------------------------------------------------------------------------
-- payment
-- ---------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment (
    id                           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_id                   VARCHAR(255)  NULL,
    invoice_id                    BIGINT        NULL,
    payment_method               VARCHAR(20)   NULL,
    gateway_order_id             VARCHAR(255)  NULL,
    gateway_payment_id           VARCHAR(255)  NULL,
    gateway_refund_id            VARCHAR(255)  NULL,
    gateway_order_id_created_at  TIMESTAMP     NULL,
    payment_status               VARCHAR(20)   NULL,
    created_at                   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                      BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_payment_payment_id       UNIQUE (payment_id),
    CONSTRAINT uq_payment_gateway_order_id UNIQUE (gateway_order_id),
    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoice (id)
) ;

CREATE INDEX idx_payment_invoiceId ON payment (invoice_id);

-- ---------------------------------------------------------------------------------------------
-- Seed data: first ROLE_ADMIN account (bootstrap problem — /user/register itself requires
-- ROLE_ADMIN, so something has to exist before the first login).
--
-- Seeded credentials:
--   username: admin@retaillite.com
--   password: admin@retaillite
-- ---------------------------------------------------------------------------------------------
INSERT INTO users (id, user_id, name, username, password, role, is_enable, created_at, updated_at)
SELECT
    -1,
    '00000000-0000-0000-0000-000000000001',
    'Root Admin',
    'admin@retaillite.com',
    '$2a$10$e9GdcOtrY2WFnLtffzD0WeQLkdiir5ZYx9UJE4i4YG2m2gLJlMCH.',
    'ROLE_ADMIN',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin@retaillite.com'
);