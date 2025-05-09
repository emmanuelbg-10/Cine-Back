package com.eviden.cine.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module javaTimeModule() {
        return new JavaTimeModule();
    }

    @Bean
    public ObjectMapper objectMapper(JavaTimeModule javaTimeModule) {
        return new ObjectMapper().registerModule(javaTimeModule);
    }
}
