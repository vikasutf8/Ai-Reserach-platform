package com.springai.airesearchagent.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "trend_analyses",
        indexes = {
                @Index(name = "idx_trend_analysis_platform", columnList = "platform"),
                @Index(name = "idx_trend_analysis_at", columnList = "analysis_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TrendAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_analysis", columnDefinition = "TEXT")
    private String rawAnalysis;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private Platform platform;

    @Column(name = "post_count", nullable = false)
    private Integer postCount;

    @CreationTimestamp
    @Column(name = "analysis_at", nullable = false, updatable = false)
    private Instant analysisAt;

    // Bidirectional convenience — owned by TrendTopic side
//    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Builder.Default
//    @ToString.Exclude
//    private List<TrendTopic> topics = new java.util.ArrayList<>();
}
