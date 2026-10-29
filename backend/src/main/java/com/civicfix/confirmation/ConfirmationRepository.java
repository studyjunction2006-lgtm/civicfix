package com.civicfix.confirmation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<CommunityConfirmation, Long> {
    boolean existsByIssueIdAndCitizenId(Long issueId, Long citizenId);
    Optional<CommunityConfirmation> findByIssueIdAndCitizenId(Long issueId, Long citizenId);
    List<CommunityConfirmation> findByIssueIdOrderByCreatedAtDesc(Long issueId);
    long countByIssueId(Long issueId);
}
