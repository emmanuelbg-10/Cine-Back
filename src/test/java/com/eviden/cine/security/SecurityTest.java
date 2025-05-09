package com.eviden.cine.security;

import com.eviden.cine.model.Role;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserRepository userRepository;

    @Test
    void testJwtAuthenticationFilterWithValidToken() throws ServletException, IOException {
        // Configurar mocks
        String token = "validToken";
        String email = "user@test.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        UserDetails fakeUserDetails = new org.springframework.security.core.userdetails.User(
                email, "password", Collections.singletonList((GrantedAuthority) () -> "ROLE_USER"));
        when(userDetailsService.loadUserByUsername(email)).thenReturn(fakeUserDetails);

        // Ejecutar filtro
        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        UserDetails principal = (UserDetails) auth.getPrincipal();
        assertEquals(email, principal.getUsername());

        // Verificar que se llama al filter chain
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testJwtAuthenticationFilterWithoutToken() throws ServletException, IOException {
        // Solicitud sin header y sin cookie
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testJwtAuthenticationFilterWithCookie() throws ServletException, IOException {
        // Token en cookie
        String token = "cookieToken";
        String email = "cookie@test.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie("jwt", token);
        request.setCookies(cookie);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        UserDetails fakeUserDetails = new org.springframework.security.core.userdetails.User(
                email, "password", Collections.singletonList((GrantedAuthority) () -> "ROLE_USER"));
        when(userDetailsService.loadUserByUsername(email)).thenReturn(fakeUserDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        UserDetails principal = (UserDetails) auth.getPrincipal();
        assertEquals(email, principal.getUsername());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testRandomPWordGenerator() {
        // Verificar que se genera una contraseña de 16 caracteres
        String pwd = RandomPWordGenerator.generate();
        assertNotNull(pwd);
        assertEquals(16, pwd.length());

        // Verificar que todos los caracteres generados pertenecen al conjunto permitido
        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (char c : pwd.toCharArray()) {
            assertTrue(allowed.indexOf(c) >= 0);
        }
    }

    @Test
    void testUserDetailsServiceImpl() {
        // Configurar usuario simulado en el repositorio
        String email = "userdetails@test.com";
        Role role = new Role();
        role.setName("ADMIN");
        User usr = User.builder()
                .email(email)
                .password("encodedPwd")
                .role(role)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(usr));

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);
        UserDetails userDetails = service.loadUserByUsername(email);
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).extracting("authority").contains("ROLE_ADMIN");
    }

    @Test
    void testUserDetailsServiceImpl_NotFound() {
        // Probar que se lanza excepción si el usuario no se encuentra
        String email = "noexiste@test.com";
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername(email));
        assertThat(exception.getMessage()).contains("Usuario no encontrado con email");
    }

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}