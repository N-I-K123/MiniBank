package org.kz.minibank.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "Email is required")
        @Email(message = "Invalid email address format")
        @Schema(
                description = "email address (used as login)",
                example = "John@Doe.xyz"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Schema(description = "User's password", example = "password123")
        String password
) {
}
