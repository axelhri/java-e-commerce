package agorafolk.api.springboot_agorafolk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePassword(
    @JsonProperty("current_password") @NotBlank(message = "Current password is required")
        String currentPassword,
    @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message =
                "Your new password must contain at least one uppercase, one lowercase, one number, and one special character")
        @JsonProperty("new_password")
        String newPassword,
    @JsonProperty("confirm_password") @NotBlank(message = "You must confirm your password")
        String confirmPassword) {}
