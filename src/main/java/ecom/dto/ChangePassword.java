package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ecom.interfaces.PasswordMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordMatch
public record ChangePassword(
    @Schema(
            description = "Current user password",
            example = "OldPassword123!",
            minLength = 8,
            format = "password")
        @JsonProperty("current_password")
        @NotBlank(message = "Current password is required")
        @Size(min = 8, message = "Current password must be at least 8 characters long")
        String currentPassword,
    @Schema(
            description = "New password",
            example = "NewPassword123!",
            minLength = 8,
            format = "password")
        @JsonProperty("new_password")
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
            message =
                "Password must contain at least one uppercase letter, one digit, and one special character.")
        String newPassword,
    @Schema(
            description = "Confirmation of the new password (must match new_password)",
            example = "NewPassword123!",
            minLength = 8,
            format = "password")
        @JsonProperty("confirm_password")
        @NotBlank(message = "You must confirm your password")
        String confirmPassword) {}
