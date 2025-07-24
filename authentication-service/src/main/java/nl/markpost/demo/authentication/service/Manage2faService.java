package nl.markpost.demo.authentication.service;

import com.bastiaanjansen.otp.SecretGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import org.apache.commons.codec.binary.Base32;
import nl.markpost.demo.authentication.api.v1.model.TOTPCode;
import nl.markpost.demo.authentication.api.v1.model.TOTPSetupResponse;
import nl.markpost.demo.authentication.api.v1.model.TOTPVerifyRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class Manage2faService {
    private static final String ISSUER = "DemoApp";
    private static final int SECRET_LENGTH = 20; // bytes
    private final UserRepository userRepository;

    @Autowired
    public Manage2faService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<TOTPSetupResponse> setup2fa() {
        String email = "user@example.com";
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        byte[] secret = SecretGenerator.generate(SECRET_LENGTH);
        String base32Secret = new Base32().encodeToString(secret).replace("=", "");
        user.setTotpSecret(base32Secret);
        user.set2faEnabled(false);
        userRepository.save(user);
        String otpauth = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                URLEncoder.encode(ISSUER, StandardCharsets.UTF_8),
                URLEncoder.encode(email, StandardCharsets.UTF_8),
                base32Secret,
                URLEncoder.encode(ISSUER, StandardCharsets.UTF_8)
        );
        TOTPSetupResponse response = new TOTPSetupResponse()
                .qrCodeUrl(otpauth)
                .secret(base32Secret);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Void> enable2fa(TOTPCode code) {
        String email = "user@example.com";
        User user = userRepository.findByEmail(email);
        if (user == null || user.getTotpSecret() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (verifyTotpCode(user.getTotpSecret(), code.getCode())) {
            user.set2faEnabled(true);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(403).build();
    }

    public ResponseEntity<Void> verify2fa(TOTPVerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || user.getTotpSecret() == null || !user.is2faEnabled()) {
            return ResponseEntity.status(403).build();
        }
        if (verifyTotpCode(user.getTotpSecret(), request.getCode())) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(403).build();
    }

    private boolean verifyTotpCode(String base32Secret, String code) {
        try {
            byte[] secret = new Base32().decode(base32Secret);
            TOTPGenerator totp = new TOTPGenerator.Builder(secret).build();
            return totp.verify(code);
        } catch (Exception e) {
            return false;
        }
    }
}
