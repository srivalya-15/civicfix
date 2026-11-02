package com.civicfix.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Complaint {

    public enum Status { SUBMITTED, VERIFIED, ASSIGNED, IN_PROGRESS, RESOLVED }
    public enum Priority { NORMAL, HIGHER, CRITICAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String complaintCode;

    private Long citizenId;
    private String citizenName;
    private String citizenPhone;
    private String issueType;
    private String severity;
    private String area;
    private String location;

    @Column(length = 1000)
    private String description;

    private String photoPath;

    @Enumerated(EnumType.STRING)
    private Status status = Status.SUBMITTED;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @ManyToOne
    private Department department;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}