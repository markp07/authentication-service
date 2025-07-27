package nl.markpost.demo.authentication.util;

import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.constant.Messages;

public class MessageResponseUtil {

public static Message createMessageResponse(Messages messages) {
  return Message.builder()
      .timestamp(java.time.OffsetDateTime.now())
      .code(messages.getCode())
      .description(messages.getDescription())
      .build();
}

}
