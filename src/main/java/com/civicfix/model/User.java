package com.civicfix.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_users")
@Data
public class User {
    public enum Role { CITIZEN, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CITIZEN;
}