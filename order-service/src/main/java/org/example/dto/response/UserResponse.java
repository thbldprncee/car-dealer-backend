package org.example.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String userType
) {
}
