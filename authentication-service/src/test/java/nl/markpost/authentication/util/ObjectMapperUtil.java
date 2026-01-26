package nl.markpost.authentication.util;

import tools.jackson.databind.ObjectMapper;

public class ObjectMapperUtil {

  public static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper;
  }
}
