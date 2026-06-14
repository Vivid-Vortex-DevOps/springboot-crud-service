CREATE TABLE products (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    description TEXT            NOT NULL DEFAULT '',
    price       NUMERIC(19, 4)  NOT NULL,
    quantity    INTEGER         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_products_price    CHECK (price >= 0),
    CONSTRAINT chk_products_quantity CHECK (quantity >= 0)
);

CREATE INDEX idx_products_name       ON products (name);
CREATE INDEX idx_products_created_at ON products (created_at DESC);
