package nl.markpost.demo.authentication.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.constant.Messages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageResponseUtilTest {

  @Test
  @DisplayName("Should create message response from Messages enum")
  void createMessageResponse_success() {
    Message message = MessageResponseUtil.createMessageResponse(Messages.LOGIN_SUCCESS);
    
    assertNotNull(message);
    assertNotNull(message.getTimestamp());
    assertEquals(Messages.LOGIN_SUCCESS.getCode(), message.getCode());
    assertEquals(Messages.LOGIN_SUCCESS.getDescription(), message.getDescription());
  }

  @Test
  @DisplayName("Should create message response with different Messages enum")
  void createMessageResponse_differentMessage() {
    Message message = MessageResponseUtil.createMessageResponse(Messages.LOGOUT_SUCCESS);
    
    assertNotNull(message);
    assertNotNull(message.getTimestamp());
    assertEquals(Messages.LOGOUT_SUCCESS.getCode(), message.getCode());
    assertEquals(Messages.LOGOUT_SUCCESS.getDescription(), message.getDescription());
  }
}
