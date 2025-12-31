package ecom.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CloudinaryResponse(
    @Schema(
            description = "Cloudinary public ID",
            example = "products/product_123/image_456",
            accessMode = Schema.AccessMode.READ_ONLY)
        String publicId,
    @Schema(
            description = "Secure URL of the uploaded image",
            example =
                "https://res.cloudinary.com/demo/image/upload/v1698400800/products/product_123/image_456.jpg",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "uri")
        String url) {}
