CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    total INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP,
    user_id UUID NOT NULL UNIQUE,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

