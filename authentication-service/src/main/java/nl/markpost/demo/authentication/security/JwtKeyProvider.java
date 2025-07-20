package nl.markpost.demo.authentication.security;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class JwtKeyProvider {

  @Getter
  private PrivateKey privateKey;

  @Getter
  private PublicKey publicKey;

  @Value("${jwt.private-key:}")
  private String privateKeyPath;

  @Value("${jwt.public-key:}")
  private String publicKeyPath;

  @PostConstruct
  public void init() throws Exception {
    if (!privateKeyPath.isEmpty() && !publicKeyPath.isEmpty()) {
      // Load keys from files
      privateKey = loadPrivateKey(privateKeyPath);
      publicKey = loadPublicKey(publicKeyPath);
    } else {
      // Generate new key pair (for dev/demo)
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      KeyPair pair = keyGen.generateKeyPair();
      privateKey = pair.getPrivate();
      publicKey = pair.getPublic();
    }
  }

  private KeyFactory getRsaKeyFactory() throws Exception {
    return KeyFactory.getInstance("RSA");
  }

  private byte[] readKeyBytes(String path, String beginMarker, String endMarker) throws Exception {
    String key;
    if (path.startsWith("classpath:")) {
      key = new String(new ClassPathResource(path.substring(10)).getInputStream().readAllBytes());
    } else {
      key = new String(Files.readAllBytes(Paths.get(path)));
    }
    key = key.replace(beginMarker, "").replace(endMarker, "").replace("\n", "");
    return Base64.getDecoder().decode(key);
  }

  private PrivateKey loadPrivateKey(String path) throws Exception {
    byte[] encoded = readKeyBytes(path, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    return getRsaKeyFactory().generatePrivate(keySpec);
  }

  private PublicKey loadPublicKey(String path) throws Exception {
    byte[] encoded = readKeyBytes(path, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    return getRsaKeyFactory().generatePublic(keySpec);
  }

  public String getPublicKeyAsPem() {
    String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
  }
}
