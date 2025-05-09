package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String password;
    private String preferredLanguage;
    private String region;
    private RoleDTO role;
    private List<String> favorites;
}