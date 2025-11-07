package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ecom.interfaces.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordMatch
public record ChangePassword(
    @JsonProperty("current_password")
        @NotBlank(message = "Current password is required")
        @Size(min = 8, message = "Current password must be at least 8 characters long")
        String currentPassword,
    @JsonProperty("new_password")
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
            message =
                "Password must contain at least one uppercase letter, one digit, and one special character.")
        String newPassword,
    @JsonProperty("confirm_password") @NotBlank(message = "You must confirm your password")
        String confirmPassword) {}
