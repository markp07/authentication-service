package nl.markpost.demo.authentication.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
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

    // Ignore null fields during serialization
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // Don't fail on unknown properties during deserialization
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    return mapper;
  }
}
