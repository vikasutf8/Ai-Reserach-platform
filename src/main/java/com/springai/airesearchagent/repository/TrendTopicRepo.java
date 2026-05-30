package com.springai.airesearchagent.repository;

import com.springai.airesearchagent.model.TrendTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendTopicRepo extends JpaRepository<TrendTopic,Long> {
}
