package nl.markpost.demo.authentication.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailsResponse(String username, String email, LocalDateTime createdAt) {}

