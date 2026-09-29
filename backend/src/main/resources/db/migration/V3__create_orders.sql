CREATE TABLE orders (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_date  DATE             NOT NULL,
    order_value DOUBLE PRECISION NOT NULL CHECK (order_value >= 0)
);

CREATE TABLE order_items (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id      INTEGER NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id    INTEGER NOT NULL REFERENCES products (id),
    item_quantity INTEGER NOT NULL CHECK (item_quantity > 0),
    -- A product appears once per order; buying more raises its quantity.
    UNIQUE (order_id, product_id)
);

-- The unique constraint already indexes order_id; this covers lookups by product.
CREATE INDEX order_items_product_id_idx ON order_items (product_id);
