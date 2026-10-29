package com.civicfix.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<ResolutionRating, Long> {
    Optional<ResolutionRating> findByIssueId(Long issueId);
    boolean existsByIssueIdAndCitizenId(Long issueId, Long citizenId);

    @Query("SELECT AVG(r.rating) FROM ResolutionRating r WHERE r.worker.id = :workerId")
    Double getAverageRatingForWorker(Long workerId);

    @Query("SELECT COUNT(r) FROM ResolutionRating r WHERE r.worker.id = :workerId")
    Long countRatingsForWorker(Long workerId);
}
