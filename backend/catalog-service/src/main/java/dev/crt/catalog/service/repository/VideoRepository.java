package dev.crt.catalog.service.repository;

import dev.crt.catalog.service.domain.Video;
import dev.crt.catalog.service.domain.VideoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    Page<Video> findByStatus(VideoStatus status, Pageable pageable);

    Page<Video> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
            String title, String description, VideoStatus status, Pageable pageable);

    Page<Video> findByCreatorIdAndStatusOrderByCreatedAtDesc(UUID creatorId, VideoStatus status, Pageable pageable);

    Page<Video> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId, Pageable pageable);

    @Query(value = "SELECT * FROM videos v " +
            "WHERE v.status = :status " +
            "AND v.id != :currentVideoId " +
            "AND (to_tsvector('english', v.title || ' ' || v.description) @@ to_tsquery('english', :combinedQuery)) " +
            "ORDER BY ts_rank(to_tsvector('english', v.title || ' ' || v.description), to_tsquery('english', :combinedQuery)) DESC",
            nativeQuery = true)
    Page<Video> findRelatedByFTS(
            @Param("combinedQuery") String combinedQuery,
            @Param("currentVideoId") UUID currentVideoId,
            @Param("status") String status,
            Pageable pageable);
}
