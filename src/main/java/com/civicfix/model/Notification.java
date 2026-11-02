package com.civicfix.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String complaintCode;
    @Column(length = 500)
    private String message;
    private LocalDateTime createdAt = LocalDateTime.now();
}
