package agorafolk.api.springboot_agorafolk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @NotBlank(message = "You must provide an email")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "You must provide a password")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}
