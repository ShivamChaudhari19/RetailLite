package in.shivam.retaillite.auth.controller;

import in.shivam.retaillite.auth.dto.AuthRequest;
import in.shivam.retaillite.auth.dto.AuthResponse;
import in.shivam.retaillite.auth.dto.RawPassword;
import in.shivam.retaillite.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor

public class AuthController {
    private final AuthService authService;
    private final PasswordEncoder encode;
    @PreAuthorize(value = "permitAll()")
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request){
        log.info("Authenticating request: {}",request.getUsername());
        return ResponseEntity.ok(authService.authenticate(request));
    }
    @PreAuthorize(value = "permitAll()")
    @PostMapping("/auth/encode")
    public ResponseEntity<String> encode(@RequestBody RawPassword password)
    {
        log.info("Encoding raw password");
        return ResponseEntity.ok(encode.encode(password.getPassword()));
    }
}
