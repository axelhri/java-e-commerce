CREATE TABLE mail_confirmations (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       token VARCHAR(255) NOT NULL UNIQUE,
                       user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       expires_at TIMESTAMP NOT NULL
);
