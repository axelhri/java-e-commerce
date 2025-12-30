ALTER TABLE products ADD COLUMN slug TEXT;

UPDATE products SET slug = 'product-' || id;

ALTER TABLE products
    ALTER COLUMN slug SET NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT unique_slug UNIQUE(slug);
