ALTER TABLE IF EXISTS orders
    ADD COLUMN IF NOT EXISTS cod_charge NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(30),
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS payment_provider VARCHAR(50),
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_provider_order_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_provider_payment_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_provider_signature VARCHAR(255);
