package com.civicfix.config;

import com.civicfix.model.*;
import com.civicfix.repository.*;
import com.civicfix.service.ComplaintService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final DepartmentRepository deptRepo;
    private final ComplaintRepository repo;
    private final UserRepository userRepo;
    private final ComplaintService service;
    private final PasswordEncoder encoder;

    @Value("${civicfix.admin.email}") private String adminEmail;
    @Value("${civicfix.admin.password}") private String adminPass;

    public DataLoader(DepartmentRepository deptRepo, ComplaintRepository repo, UserRepository userRepo,
                      ComplaintService service, PasswordEncoder encoder) {
        this.deptRepo = deptRepo;
        this.repo = repo;
        this.userRepo = userRepo;
        this.service = service;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepo.existsByEmail(adminEmail)) {
            User a = new User();
            a.setName("Municipal Admin");
            a.setEmail(adminEmail);
            a.setPasswordHash(encoder.encode(adminPass));
            a.setRole(User.Role.ADMIN);
            userRepo.save(a);
        }

        User demo = userRepo.findByEmail("citizen@civicfix.com").orElseGet(() -> {
            User u = new User();
            u.setName("Demo Citizen");
            u.setEmail("citizen@civicfix.com");
            u.setPhone("9000000000");
            u.setPasswordHash(encoder.encode("citizen123"));
            u.setRole(User.Role.CITIZEN);
            return userRepo.save(u);
        });

        if (deptRepo.count() == 0) {
            deptRepo.saveAll(List.of(
                    new Department("Roads & Transport"),
                    new Department("Sanitation & Waste"),
                    new Department("Water Supply"),
                    new Department("Electrical / Streetlights"),
                    new Department("Drainage & Sewerage")));
        }

        if (repo.count() == 0) {
            Long id = demo.getId();
            String n = demo.getName(), p = demo.getPhone();
            service.create(id, n, p, "Pothole", "Madhapur", "Near Metro Pillar 1100", "HIGH", "Deep pothole causing accidents.", null);
            service.create(id, n, p, "Pothole", "Madhapur", "Image Gardens Road", "MEDIUM", "Large pothole after rain.", null);
            service.create(id, n, p, "Pothole", "Madhapur", "Hitech City Junction", "MEDIUM", "Road surface broken.", null);
            service.create(id, n, p, "Broken Streetlight", "Kukatpally", "KPHB Phase 3", "LOW", "Streetlight off for a week.", null);
            service.create(id, n, p, "Uncollected Garbage", "Gachibowli", "Near Bus Stop", "HIGH", "Garbage not collected for 5 days.", null);
        }
    }
}
