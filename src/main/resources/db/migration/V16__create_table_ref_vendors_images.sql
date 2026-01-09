CREATE TABLE vendors_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    image_url VARCHAR NOT NULL,
    cloudinary_image_id VARCHAR NOT NULL,
    vendor_id UUID NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
