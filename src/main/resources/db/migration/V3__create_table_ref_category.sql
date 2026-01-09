CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE category_parent (
    child_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    parent_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE (child_id, parent_id)
);