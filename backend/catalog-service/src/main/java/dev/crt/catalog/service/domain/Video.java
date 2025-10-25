package dev.crt.catalog.service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "videos")
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Video {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false, updatable = false)
    private UUID creatorId;

    @Column(nullable = false)
    private String thumbnailUrl;

    @Column(nullable = false)
    private String originalVideoUrl;

    @Column()
    private String duration;

    @Enumerated(EnumType.STRING)
    private VideoStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> videoUrls;

    @PrePersist
    private void generateIdAndCreatedDate(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}
