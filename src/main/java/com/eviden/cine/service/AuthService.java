package com.eviden.cine.service;

import com.eviden.cine.dtos.AuthRequestDTO;
import com.eviden.cine.dtos.AuthResponseDTO;
import com.eviden.cine.dtos.RegisterRequestDTO;
import com.eviden.cine.model.Role;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.RoleRepository;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("El rol USER no existe en la base de datos"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .preferredLanguage(request.getPreferredLanguage())
                .region(request.getRegion())
                .role(userRole)
                .build();

        return userRepository.save(user);
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponseDTO(token);
    }
}
