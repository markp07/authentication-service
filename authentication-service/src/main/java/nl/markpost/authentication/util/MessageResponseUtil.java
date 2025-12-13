package nl.markpost.authentication.util;

import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.constant.Messages;

public class MessageResponseUtil {

  public static Message createMessageResponse(Messages messages) {
    return Message.builder()
        .timestamp(java.time.OffsetDateTime.now())
        .code(messages.getCode())
        .description(messages.getDescription())
        .build();
  }

}
