CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    updated_at TIMESTAMP,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
);

