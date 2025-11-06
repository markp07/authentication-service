package nl.markpost.demo.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDetailsResponse(String username, String email, LocalDateTime createdAt) {

}

