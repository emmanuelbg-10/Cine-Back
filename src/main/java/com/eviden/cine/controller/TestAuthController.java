package com.eviden.cine.controller;

import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestAuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/tokens")
    public ResponseEntity<Map<String, String>> getAllSeededTokens() {
        Map<String, String> tokens = new LinkedHashMap<>();

        String[] emails = {
                "juanito@mail.com",
                "marta@mail.com",
                "carlos89@mail.com",
                "lucia23@mail.com",
                "adminpro@mail.com"
        };

        for (String email : emails) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                String token = jwtUtil.generateToken(userOpt.get());
                tokens.put(email, "Bearer " + token);
            } else {
                tokens.put(email, "Usuario no encontrado");
            }
        }

        return ResponseEntity.ok(tokens);
    }
}
