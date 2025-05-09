package com.eviden.cine.component;

import com.eviden.cine.model.User;
import com.eviden.cine.security.JwtUtil;
import com.eviden.cine.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService; // Tu clase de usuarios

    public CustomOAuth2SuccessHandler(JwtUtil jwtUtil, @Lazy UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        System.out.println("CustomOAuth2SuccessHandler initialized");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        User user = userService.findOrCreateUser(email, oAuth2User);

        String jwt = jwtUtil.generateToken(user);
        System.out.println("JWT generado: " + jwt);

        // Devuelve el token como JSON
        response.sendRedirect("http://localhost:5173?token="+jwt);
    }

}
