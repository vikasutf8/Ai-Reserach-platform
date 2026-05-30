package com.springai.airesearchagent.scraper;

import com.springai.airesearchagent.model.Platform;
import com.springai.airesearchagent.model.ScrapedPost;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RedditScraper extends AbstractScraper implements PlatformScraper{
    @Override
    public Platform getPlatform() {
        return Platform.REDDIT;
    }

    @Override
    public List<ScrapedPost> scrapeTopPosts() {
        final  String proxyIp = detectProxyIp();
        log.info("Using proxy IP: {}", proxyIp);

        return List.of();
    }
}
