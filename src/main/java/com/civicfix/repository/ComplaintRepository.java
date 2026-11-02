// ComplaintRepository.java
package com.civicfix.repository;

import com.civicfix.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByComplaintCode(String code);
    List<Complaint> findAllByOrderByCreatedAtDesc();
    List<Complaint> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);
    List<Complaint> findByAreaIgnoreCaseAndIssueTypeAndStatusNot(String area, String issueType, Complaint.Status status);
    long countByStatus(Complaint.Status status);
    long countByPriority(Complaint.Priority priority);
}
