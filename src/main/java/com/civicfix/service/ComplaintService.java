package com.civicfix.service;

import com.civicfix.model.*;
import com.civicfix.model.Complaint.Priority;
import com.civicfix.model.Complaint.Status;
import com.civicfix.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository repo;
    private final NotificationRepository notifRepo;
    private final DepartmentRepository deptRepo;

    @Value("${civicfix.upload-dir}")
    private String uploadDir;

    public ComplaintService(ComplaintRepository repo, NotificationRepository notifRepo, DepartmentRepository deptRepo) {
        this.repo = repo;
        this.notifRepo = notifRepo;
        this.deptRepo = deptRepo;
    }

    @Transactional
    public Complaint create(Long citizenId, String name, String phone, String type, String area, String location,
                            String severity, String description, MultipartFile photo) throws IOException {
        Complaint c = new Complaint();
        c.setCitizenId(citizenId);
        c.setCitizenName(name);
        c.setCitizenPhone(phone);
        c.setIssueType(type);
        c.setArea(area.trim());
        c.setLocation(location);
        c.setSeverity(severity.toUpperCase());
        c.setDescription(description);

        if (photo != null && !photo.isEmpty()) {
            String ct = photo.getContentType();
            if (ct == null || !ct.startsWith("image/"))
                throw new IllegalArgumentException("Only image files are allowed");
            Files.createDirectories(Path.of(uploadDir));
            String safeName = UUID.randomUUID() + "_" + photo.getOriginalFilename().replaceAll("[^a-zA-Z0-9.]", "_");
            Files.copy(photo.getInputStream(), Path.of(uploadDir, safeName));
            c.setPhotoPath("/uploads/" + safeName);
        }

        c = repo.save(c);
        c.setComplaintCode(String.format("CF%05d", c.getId()));
        recalcPriority(c.getArea(), c.getIssueType());
        notify(c, "Your complaint " + c.getComplaintCode() + " has been submitted successfully.");
        return c;
    }

    @Transactional
    public Complaint updateStatus(String code, String newStatus, Long departmentId) {
        Complaint c = repo.findByComplaintCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        Status next;
        try { next = Status.valueOf(newStatus); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid status"); }

        if (next.ordinal() != c.getStatus().ordinal() + 1)
            throw new IllegalArgumentException("Invalid move. Next allowed status is: "
                    + (c.getStatus() == Status.RESOLVED ? "none" : Status.values()[c.getStatus().ordinal() + 1]));

        if (next == Status.ASSIGNED) {
            if (departmentId == null) throw new IllegalArgumentException("Select a department to assign");
            c.setDepartment(deptRepo.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found")));
        }

        c.setStatus(next);
        c.setUpdatedAt(LocalDateTime.now());

        String msg = switch (next) {
            case VERIFIED -> "Your complaint has been verified by the municipal office.";
            case ASSIGNED -> "Your complaint has been assigned to the " + c.getDepartment().getName() + " department.";
            case IN_PROGRESS -> "Work has started on your complaint.";
            case RESOLVED -> "Good news! Your complaint has been resolved. Thank you for helping improve the city.";
            default -> "Status updated.";
        };
        notify(c, msg);

        if (next == Status.RESOLVED) recalcPriority(c.getArea(), c.getIssueType());
        return c;
    }

    /** Priority = severity points + points for number of open similar complaints in the same area. */
    private void recalcPriority(String area, String type) {
        List<Complaint> group = repo.findByAreaIgnoreCaseAndIssueTypeAndStatusNot(area, type, Status.RESOLVED);
        int count = group.size();
        int areaPoints = count >= 5 ? 3 : count >= 3 ? 2 : count >= 2 ? 1 : 0;
        for (Complaint x : group) {
            int sevPoints = switch (x.getSeverity()) { case "HIGH" -> 3; case "MEDIUM" -> 2; default -> 1; };
            int score = sevPoints + areaPoints;
            x.setPriority(score >= 5 ? Priority.CRITICAL : score >= 3 ? Priority.HIGHER : Priority.NORMAL);
        }
        repo.saveAll(group);
    }

    private void notify(Complaint c, String message) {
        Notification n = new Notification();
        n.setComplaintCode(c.getComplaintCode());
        n.setMessage(message);
        notifRepo.save(n);
    }
}
