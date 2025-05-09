package com.eviden.cine.service;

import com.eviden.cine.dtos.UpdateProfileDTO;
import com.eviden.cine.model.Role;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.RoleRepository;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.security.RandomPWordGenerator;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RandomPWordGenerator randomPWordGenerator;

    public Map<String, String> getResetTokens() {
        return resetTokens;
    }

    private final Map<String, String> resetTokens = new HashMap<>();

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService, RandomPWordGenerator randomPWordGenerator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.randomPWordGenerator = randomPWordGenerator;
    }

    public User changeUserRoleById(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Role newRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User updateProfile(UpdateProfileDTO dto, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setPreferredLanguage(dto.getPreferredLanguage());
        user.setRegion(dto.getRegion());

        return userRepository.save(user);
    }

    public void sendResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ese email"));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);

        try {
            emailService.sendPasswordResetEmail(email, token);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de recuperación", e);
        }
    }

    public void resetPassword(String token, String newPassword) {
        String email = resetTokens.get(token);
        if (email == null) {
            throw new RuntimeException("Token inválido o expirado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(token);
    }

    public User findOrCreateUser(String email, OAuth2User oAuth2User) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            String randomPassword = randomPWordGenerator.generate();
            newUser.setEmail(email);
            newUser.setUsername(oAuth2User.getAttribute("name"));
            newUser.setPassword(passwordEncoder.encode(randomPassword));
            newUser.setRole(roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Role USER not found")));
            return userRepository.save(newUser);
        });
    }

}
