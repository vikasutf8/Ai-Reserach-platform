package com.springai.airesearchagent.scraper;

import com.springai.airesearchagent.config.ProxyConfig;
import com.springai.airesearchagent.model.Platform;
import com.springai.airesearchagent.model.ScrapedPost;
import com.springai.airesearchagent.repository.ScrapedPostRepo;
import com.springai.airesearchagent.scraper.exception.ScraperException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class RedditScraper extends AbstractScraper implements PlatformScraper{

    private final ScrapedPostRepo scrapedPostRepo;
    private final ObjectMapper objectMapper;

    public RedditScraper(ProxyConfig proxyConfig, final ScrapedPostRepo scrapedPostRepo, final ObjectMapper objectMapper) {
        super(proxyConfig);
        this.scrapedPostRepo = scrapedPostRepo;
        this.objectMapper = objectMapper;
    }



    @Value("${scraping.reddit.max-posts-per-subreddit:100}")
    private int maxPostsPerSubreddit;

    @Value("${scraping.reddit.request-delay-ms:1000}")
    private int requestDelayMs;

    @Value("${scraping.reddit.subreddits}")
    private List<String> subreddits;

    // Reddit JSON API — no auth needed for public subreddits
    private static final String REDDIT_URL = "https://www.reddit.com/r/%s/top.json?limit=%d&t=day";


    @Override
    public Platform getPlatform() {
        return Platform.REDDIT;
    }

    @Override
    public List<ScrapedPost> scrapeTopPosts() {
        final String proxyIp = detectProxyIp();
        log.info("Reddit scrape started | proxy: {} | subreddits: {}", proxyIp, subreddits);

        final List<ScrapedPost> allSaved = new ArrayList<>();

        for (String subreddit : subreddits) {
            try {
                List<ScrapedPost> saved = scrapeSubreddit(subreddit, proxyIp);
                allSaved.addAll(saved);
                log.info("r/{} → {} new posts saved", subreddit, saved.size());

                // Respect Reddit rate limit between subreddits
                Thread.sleep(requestDelayMs);

            } catch (ScraperException e) {
                // One subreddit failing must not stop the rest
                log.error("Failed to scrape r/{} — reason: {} | msg: {}",
                        subreddit, e.getReason(), e.getMessage());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupt flag
                log.warn("Scraping interrupted during delay after r/{}", subreddit);
                break;
            }
        }

        log.info("Reddit scrape completed — total new posts saved: {}", allSaved.size());
        return allSaved;
    }

    private List<ScrapedPost> scrapeSubreddit(final String subreddit, final String proxyIp) {
        final String url  = String.format(REDDIT_URL, subreddit, maxPostsPerSubreddit);
        final String json = fetchUrl(url);  // throws ScraperException on failure

        return parseAndSave(json, subreddit, proxyIp);
    }

    private List<ScrapedPost> parseAndSave(final String json,
                                           final String subreddit,
                                           final String proxyIp) {
        final List<ScrapedPost> saved = new ArrayList<>();

        try {
            JsonNode root     = objectMapper.readTree(json);
            JsonNode children = root.path("data").path("children");

            for (JsonNode child : children) {
                JsonNode data = child.path("data");

                String externalId = data.path("id").asText();

                // ── dedup check against uk_external_id_platform ──────────────
                if (scrapedPostRepo.existsByExternalIdAndPlatform(externalId, Platform.REDDIT)) {
                    log.debug("Skipping duplicate — externalId: {} platform: REDDIT", externalId);
                    continue;
                }

                ScrapedPost post = ScrapedPost.builder()
                        .platform(Platform.REDDIT)
                        .externalId(externalId)
                        .title(data.path("title").asText())
                        .content(data.path("selftext").asText(null))   // null if empty
                        .url(data.path("url").asText())
                        .author(data.path("author").asText())
                        .score(data.path("score").asInt(0))
                        .commentCount(data.path("num_comments").asInt(0))
                        .subReddit(subreddit)
                        .proxyIpUsed(proxyIp)
                        .postedAt(Instant.ofEpochSecond(data.path("created_utc").asLong()))
                        .build();

                saved.add(scrapedPostRepo.save(post));
            }

        } catch (Exception e) {
            // JSON parse failure — log and skip this subreddit's batch
            log.error("Failed to parse Reddit JSON for r/{}: {}", subreddit, e.getMessage());
        }

        return saved;
    }
}
