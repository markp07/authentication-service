package nl.markpost.demo.authentication.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtKeyProviderTest {

  @Test
  @DisplayName("Should generate key pair if no paths are provided")
  void init_generatesKeyPair() throws Exception {
    JwtKeyProvider provider = new JwtKeyProvider();
    ReflectionTestUtils.setField(provider, "privateKeyPath", "");
    ReflectionTestUtils.setField(provider, "publicKeyPath", "");
    provider.init();
    assertNotNull(provider.getPrivateKey());
    assertNotNull(provider.getPublicKey());
  }

  @Test
  @DisplayName("Should return PEM string for public key")
  void getPublicKeyAsPem_returnsPem() throws Exception {
    JwtKeyProvider provider = new JwtKeyProvider();
    ReflectionTestUtils.setField(provider, "privateKeyPath", "");
    ReflectionTestUtils.setField(provider, "publicKeyPath", "");
    provider.init();
    String pem = provider.getPublicKeyAsPem();
    assertTrue(pem.startsWith("-----BEGIN PUBLIC KEY-----"));
    assertTrue(pem.endsWith("-----END PUBLIC KEY-----"));
  }

}
