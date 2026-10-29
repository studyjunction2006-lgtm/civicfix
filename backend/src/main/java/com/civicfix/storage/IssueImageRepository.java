package com.civicfix.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueImageRepository extends JpaRepository<IssueImage, Long> {
    List<IssueImage> findByIssueId(Long issueId);
    List<IssueImage> findByIssueIdAndImageType(Long issueId, IssueImageType imageType);
}
