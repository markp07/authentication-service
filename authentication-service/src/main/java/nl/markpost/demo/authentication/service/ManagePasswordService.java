package nl.markpost.demo.authentication.service;

import nl.markpost.demo.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.ForgotPasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ManagePasswordService {
    public ResponseEntity<Void> changePassword(ChangePasswordRequest request) {
        // TODO: Implement password change logic
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> forgotPassword(ForgotPasswordRequest request) {
        // TODO: Implement forgot password logic (send email)
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> resetPassword(ResetPasswordRequest request) {
        // TODO: Implement password reset logic
        return ResponseEntity.ok().build();
    }
}

