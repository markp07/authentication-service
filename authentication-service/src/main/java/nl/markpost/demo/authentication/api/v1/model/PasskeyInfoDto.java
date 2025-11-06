package nl.markpost.demo.authentication.api.v1.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasskeyInfoDto {
    private String credentialId;
    private String name;
    private LocalDateTime createdAt;
}

