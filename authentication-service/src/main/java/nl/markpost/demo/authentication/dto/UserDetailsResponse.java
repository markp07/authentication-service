package nl.markpost.demo.authentication.dto;

import java.time.LocalDateTime;

public record UserDetailsResponse(String username, String email, LocalDateTime createdAt) {

}

