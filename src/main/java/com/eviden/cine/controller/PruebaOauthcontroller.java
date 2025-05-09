package com.eviden.cine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PruebaOauthcontroller {
    @GetMapping("/prueba")
    public ResponseEntity<String> prueba(Authentication auth) {
        return ResponseEntity.ok("Autenticado como: " + auth.getName());
    }

}
