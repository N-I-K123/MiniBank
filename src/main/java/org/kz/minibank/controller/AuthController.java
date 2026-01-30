package org.kz.minibank.controller;

import jakarta.validation.Valid;
import org.kz.minibank.DTO.AuthResponse;
import org.kz.minibank.DTO.CreateUserDTO;
import org.kz.minibank.DTO.LoginRequest;
import org.kz.minibank.model.User;
import org.kz.minibank.repository.UserRepository;
import org.kz.minibank.security.JwtService;
import org.kz.minibank.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserDTO request){
        User user = userService.registerUser(request.name(), request.surname(), request.email(), passwordEncoder.encode(request.password()));

        String jwt = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@Valid @RequestBody LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userService.getUserByEmail(request.email());

        String jwt = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

}
