package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "role")
    @JsonBackReference(value = "role-users")
    @ToString.Exclude
    private List<User> users;
}
