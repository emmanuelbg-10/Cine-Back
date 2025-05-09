package com.eviden.cine.component;

import com.eviden.cine.model.User;
import com.eviden.cine.security.JwtUtil;
import com.eviden.cine.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.mockito.Mockito.*;

public class CustomOAuth2SuccessHandlerTest {

    private CustomOAuth2SuccessHandler successHandler;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        successHandler = new CustomOAuth2SuccessHandler(jwtUtil, userService);
    }

    @Test
    public void testOnAuthenticationSuccess() throws Exception {
        String email = "test@example.com";
        String token = "jwt-token";

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(userService.findOrCreateUser(email, oAuth2User)).thenReturn(user);
        when(jwtUtil.generateToken(user)).thenReturn(token);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userService, times(1)).findOrCreateUser(email, oAuth2User);
        verify(jwtUtil, times(1)).generateToken(user);
        verify(response, times(1)).sendRedirect("http://localhost:5173?token=" + token);
    }
}