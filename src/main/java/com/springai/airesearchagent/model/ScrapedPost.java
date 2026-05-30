package com.springai.airesearchagent.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "scraped_posts",
        indexes = {
                @Index(name = "idx_scraped_posts_platform", columnList = "platform"),
                @Index(name = "idx_scraped_posts_external_id_platform", columnList = "external_id, platform"
//                        unique = true
                ), // make sure don;t have duplicate entry marked unique true at index level
                @Index(name = "idx_scraped_posts_posted_at", columnList = "posted_at"),
                @Index(name = "idx_scraped_posts_subreddit", columnList = "sub_reddit"),
                @Index(name = "idx_scraped_posts_score", columnList = "score")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_external_id_platform",
                        columnNames = {"external_id","platform"}
                )
        }

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "content") // content can be huge — exclude from logs
public class ScrapedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private Platform platform;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(name = "title", nullable = false, length = 1000)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "score")
    private Integer score;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "sub_reddit", length = 255)
    private String subReddit;

    @Column(name = "proxy_ip_used", length = 50)
    private String proxyIpUsed;

    @Column(name = "posted_at")
    private Instant postedAt;

    @CreationTimestamp
    @Column(name = "scraped_at", nullable = false, updatable = false)
    private Instant scrapedAt;


}
