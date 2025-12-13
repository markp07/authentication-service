package nl.markpost.authentication.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // Add JDK8 module to handle Optional types
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new JsonNullableModule());

    // Add JavaTimeModule to handle Java 8 date/time types like OffsetDateTime
    mapper.registerModule(new JavaTimeModule());

    // Write dates as ISO-8601 strings instead of timestamps
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Ignore null fields during serialization
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // Don't fail on unknown properties during deserialization
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return mapper;
  }
}
