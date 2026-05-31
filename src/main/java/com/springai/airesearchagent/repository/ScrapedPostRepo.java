package com.springai.airesearchagent.repository;

import com.springai.airesearchagent.model.Platform;
import com.springai.airesearchagent.model.ScrapedPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapedPostRepo  extends JpaRepository<ScrapedPost,Long> {
    boolean existsByExternalIdAndPlatform(String externalId, Platform platform);
}
