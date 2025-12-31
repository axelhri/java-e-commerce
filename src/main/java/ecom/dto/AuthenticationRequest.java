package ecom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
    @Schema(description = "User email address", example = "john.doe@example.com")
        @NotBlank(message = "You must provide an email")
        @Email(message = "Invalid email")
        String email,
    @Schema(
            description = "User password",
            example = "Password123!",
            minLength = 8,
            format = "password")
        @NotBlank(message = "You must provide a password")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message =
                "Password must contain at least one uppercase, one lowercase, one number, and one special character")
        String password) {}
