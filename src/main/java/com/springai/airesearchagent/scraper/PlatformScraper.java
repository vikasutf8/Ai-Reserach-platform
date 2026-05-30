package com.springai.airesearchagent.scraper;

import com.springai.airesearchagent.model.Platform;
import com.springai.airesearchagent.model.ScrapedPost;

import java.util.List;

public interface PlatformScraper {

    Platform getPlatform();

    List<ScrapedPost> scrapeTopPosts();
}
