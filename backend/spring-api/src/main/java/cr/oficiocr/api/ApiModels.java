package cr.oficiocr.api;

import java.time.Instant;
import java.util.UUID;

record RegisterRequest(String name, String email, String password) {}
record LoginRequest(String email, String password) {}
record EmailRequest(String email) {}
record ResetPasswordRequest(String email, String token, String password) {}
record UpdateProfileRequest(String name, String email) {}
record CreateJobRequest(String title, String description, String category, String location) {}
record UserResponse(UUID id, String name, String email, String role) {
    UserResponse(UserEntity u) { this(u.id,u.name,u.email,u.role); }
}
record AdminUserResponse(UUID id, String name, String email, String role, Instant createdAt) {
    AdminUserResponse(UserEntity u) { this(u.id,u.name,u.email,u.role,u.createdAt); }
}
record SessionResponse(Instant expiresAt, UserResponse user) {}
record MessageResponse(String message) {}
