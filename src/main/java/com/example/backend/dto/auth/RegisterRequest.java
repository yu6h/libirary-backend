package com.example.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{8,20}$", message = "手機號碼格式不正確")
        String phoneNumber,

        @NotBlank
        @Size(min = 3, max = 50)
        String userName,

        @NotBlank
        @Size(min = 6, max = 100)
        String password
) {
}
