package com.eviden.cine.controller;

import com.eviden.cine.model.Classification;
import com.eviden.cine.security.JwtUtil;
import com.eviden.cine.security.UserDetailsServiceImpl;
import com.eviden.cine.service.ClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClassificationController.class)
@Import({ClassificationControllerTest.MockConfig.class, com.eviden.cine.config.SecurityConfig.class})
class ClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClassificationService classificationService;

    private Classification classification;

    @BeforeEach
    void setUp() {
        classification = new Classification();
        classification.setId(1);
        classification.setName("PG-13");
    }

    @WithMockUser
    @Test
    void testGetAllClassifications() throws Exception {
        when(classificationService.getAllClassifications()).thenReturn(List.of(classification));

        mockMvc.perform(get("/api/classifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("PG-13"));

        verify(classificationService).getAllClassifications();
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ClassificationService classificationService() {
            return Mockito.mock(ClassificationService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
        }

        @Bean
        public UserDetailsServiceImpl userDetailsService() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }
}
