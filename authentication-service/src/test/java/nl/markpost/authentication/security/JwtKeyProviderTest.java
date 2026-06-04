package nl.markpost.authentication.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtKeyProviderTest {

  @Test
  @DisplayName("Should fail startup when key paths are missing")
  void init_failsWhenPathsMissing() {
    JwtKeyProvider provider = new JwtKeyProvider();
    ReflectionTestUtils.setField(provider, "privateKeyPath", "");
    ReflectionTestUtils.setField(provider, "publicKeyPath", "");

    IllegalStateException exception = assertThrows(IllegalStateException.class, provider::init);
    assertTrue(exception.getMessage().contains("jwt.private-key"));
  }

  @Test
  @DisplayName("Should fail startup when key files are invalid")
  void init_failsWhenKeyContentInvalid() throws Exception {
    JwtKeyProvider provider = new JwtKeyProvider();
    Path privateKey = SecurityTestUtils.createTempPemFile("private-key", "not-a-private-key");
    Path publicKey = SecurityTestUtils.createTempPemFile("public-key", "not-a-public-key");

    ReflectionTestUtils.setField(provider, "privateKeyPath", privateKey.toString());
    ReflectionTestUtils.setField(provider, "publicKeyPath", publicKey.toString());

    assertThrows(IllegalStateException.class, provider::init);
  }

  @Test
  @DisplayName("Should load configured classpath key files")
  void init_loadsConfiguredKeys() throws Exception {
    JwtKeyProvider provider = new JwtKeyProvider();
    ReflectionTestUtils.setField(provider, "privateKeyPath", SecurityTestUtils.TEST_PRIVATE_KEY_PATH);
    ReflectionTestUtils.setField(provider, "publicKeyPath", SecurityTestUtils.TEST_PUBLIC_KEY_PATH);

    provider.init();

    assertNotNull(provider.getPrivateKey());
    assertNotNull(provider.getPublicKey());
  }

  @Test
  @DisplayName("Should return PEM string for public key")
  void getPublicKeyAsPem_returnsPem() throws Exception {
    JwtKeyProvider provider = new JwtKeyProvider();
    ReflectionTestUtils.setField(provider, "privateKeyPath", SecurityTestUtils.TEST_PRIVATE_KEY_PATH);
    ReflectionTestUtils.setField(provider, "publicKeyPath", SecurityTestUtils.TEST_PUBLIC_KEY_PATH);
    provider.init();
    String pem = provider.getPublicKeyAsPem();
    assertTrue(pem.startsWith("-----BEGIN PUBLIC KEY-----"));
    assertTrue(pem.endsWith("-----END PUBLIC KEY-----"));
  }

}
