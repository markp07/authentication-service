package nl.markpost.demo.authentication.controller;

import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.controller.Manage2faApi;
import nl.markpost.demo.authentication.service.Manage2faService;
import nl.markpost.demo.authentication.api.v1.model.TOTPCode;
import nl.markpost.demo.authentication.api.v1.model.TOTPSetupResponse;
import nl.markpost.demo.authentication.api.v1.model.TOTPVerifyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class Manage2faController implements Manage2faApi {

    private final Manage2faService manage2faService;

    @Override
    public ResponseEntity<TOTPSetupResponse> setup2FA() {
        return manage2faService.setup2fa();
    }

    @Override
    public ResponseEntity<Void> enable2FA(TOTPCode code) {
        return manage2faService.enable2fa(code);
    }

    @Override
    public ResponseEntity<Void> verify2FA(TOTPVerifyRequest request) {
        return manage2faService.verify2fa(request);
    }
}

