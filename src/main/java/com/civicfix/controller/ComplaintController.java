package com.civicfix.controller;

import com.civicfix.model.*;
import com.civicfix.repository.*;
import com.civicfix.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    private final ComplaintService service;
    private final ComplaintRepository repo;
    private final NotificationRepository notifRepo;
    private final DepartmentRepository deptRepo;
    private final UserRepository userRepo;

    public ComplaintController(ComplaintService service, ComplaintRepository repo, NotificationRepository notifRepo,
                               DepartmentRepository deptRepo, UserRepository userRepo) {
        this.service = service;
        this.repo = repo;
        this.notifRepo = notifRepo;
        this.deptRepo = deptRepo;
        this.userRepo = userRepo;
    }

    @PostMapping(value = "/complaints", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Complaint create(@RequestParam String issueType,
                            @RequestParam String severity,
                            @RequestParam String area,
                            @RequestParam String location,
                            @RequestParam String description,
                            @RequestParam(required = false) MultipartFile photo,
                            HttpSession session) throws IOException {
        Long uid = (Long) session.getAttribute("uid");
        User u = userRepo.findById(uid).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return service.create(u.getId(), u.getName(), u.getPhone(), issueType, area, location, severity, description, photo);
    }

    @GetMapping("/my/complaints")
    public List<Complaint> mine(HttpSession session) {
        return repo.findByCitizenIdOrderByCreatedAtDesc((Long) session.getAttribute("uid"));
    }

    @GetMapping("/complaints/{code}")
    public Complaint get(@PathVariable String code) {
        return repo.findByComplaintCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
    }

    @GetMapping("/complaints/{code}/notifications")
    public List<Notification> notifications(@PathVariable String code) {
        return notifRepo.findByComplaintCodeOrderByCreatedAtDesc(code.trim().toUpperCase());
    }

    @GetMapping("/departments")
    public List<Department> departments() {
        return deptRepo.findAll();
    }
}
