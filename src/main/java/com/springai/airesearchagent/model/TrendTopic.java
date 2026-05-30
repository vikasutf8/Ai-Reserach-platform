package com.springai.airesearchagent.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "trend_topics",
        indexes = {
                @Index(name = "idx_trend_topic_platform", columnList = "primary_platform"),
                @Index(name = "idx_trend_topic_category", columnList = "category"),
                @Index(name = "idx_trend_topic_score", columnList = "score"),
                @Index(name = "idx_trend_topic_analysis_id", columnList = "analysis_id"),
                @Index(name = "idx_trend_topic_detected_at", columnList = "detected_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "analysis")
public class TrendTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic", nullable = false, length = 500)
    private String topic;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "reasoning", columnDefinition = "TEXT") // LLM-generated — can be verbose
    private String reasoning;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "mention_count")
    private Integer mentionCount;

    @Column(name = "score")
    private Double score;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_platform", nullable = false, length = 20)
    private Platform primaryPlatform;


    @Column(name = "sample_post_ids", columnDefinition = "TEXT", length = 2048)
    private String samplePostIds;

// Bidirectional
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "analysis_id", nullable = false,
//            foreignKey = @ForeignKey(name = "fk_trend_topic_analysis"))
//    private TrendAnalysis analysis;

// unidirectional
    @ManyToOne
    @JoinColumn(name = "analysis_id")
    private TrendAnalysis analysis;

    @CreationTimestamp
    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

}
