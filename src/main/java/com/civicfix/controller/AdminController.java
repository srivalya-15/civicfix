package com.civicfix.controller;

import com.civicfix.model.Complaint;
import com.civicfix.repository.ComplaintRepository;
import com.civicfix.service.ComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    public record StatusUpdate(String status, Long departmentId) { }

    private final ComplaintRepository repo;
    private final ComplaintService service;

    public AdminController(ComplaintRepository repo, ComplaintService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping("/complaints")
    public List<Complaint> all() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", repo.count());
        for (Complaint.Status s : Complaint.Status.values()) m.put(s.name(), repo.countByStatus(s));
        m.put("critical", repo.countByPriority(Complaint.Priority.CRITICAL));
        return m;
    }

    @PutMapping("/complaints/{code}/status")
    public Complaint update(@PathVariable String code, @RequestBody StatusUpdate body) {
        return service.updateStatus(code, body.status(), body.departmentId());
    }
}
