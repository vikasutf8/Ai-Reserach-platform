package com.springai.airesearchagent.scraper;


import com.springai.airesearchagent.config.ProxyConfig;
import com.springai.airesearchagent.scraper.exception.ScraperException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class AbstractScraper {

    private final ProxyConfig proxyConfig;

//    public String detectProxyIp(){
//        return "127.0.0.1";
//    }
//    public String fetchUrl(final String url){
//        // Placeholder for actual HTTP request logic using proxy and user agent
//        return "";
//    }

    public String detectUserAgent(){
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    }


    /**
     * Builds a shared OkHttpClient wired with proxy + proxy auth.
     * Call once and reuse — OkHttpClient is heavyweight (thread pool, conn pool).
     */
    public OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .proxy(proxyConfig.toProxy())
                .proxyAuthenticator(proxyAuthenticator())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Handles 407 Proxy Authentication Required.
     * OkHttp calls this automatically when the proxy returns 407.
     */
    private Authenticator proxyAuthenticator() {
        return (route, response) -> {
            if (responseCount(response) >= 3) {
                log.error("Proxy auth failed after 3 attempts — check credentials");
                return null;  // null = give up, stop retrying
            }

            String credential = okhttp3.Credentials.basic(
                    proxyConfig.getUsername(),
                    proxyConfig.getPassword()
            );


            return response.request()
                    .newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
    }

    /**
     * Counts how many times this request has been retried.
     * Prevents infinite 407 retry loops if credentials are wrong.
     */
    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }


    /**
     * Detects the actual outbound IP being used (via proxy if configured).
     * Hits ipify — lightweight, returns plain-text IP, no JSON parsing needed.
     */
    public String detectProxyIp() {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.ipify.org")
                .header("User-Agent", detectUserAgent())
                .build();

        try (okhttp3.Response response = buildHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("detectProxyIp: unexpected response code {}", response.code());
                return "UNKNOWN";
            }
            String ip = response.body().string().trim();
            log.info("Outbound proxy IP detected: {}", ip);
            return ip;

        } catch (IOException e) {
            log.error("detectProxyIp: failed to reach ipify — {}", e.getMessage());
            return "UNKNOWN";
        }
    }


    /**
     * Fetches raw HTML/JSON from the given URL via configured proxy.
     *
     * @param url target URL to scrape
     * @return response body as String
     * @throws ScraperException on non-2xx, empty body, or network failure
     */
    public String fetchUrl(final String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("fetchUrl: url must not be blank");
        }

        log.debug("Fetching URL: {}", url);

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", detectUserAgent())
                .header("Accept", "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build();

        try (okhttp3.Response response = buildHttpClient().newCall(request).execute()) {

            if (response.code() == 429) {
                log.warn("Rate limited (429) on URL: {}", url);
                throw new ScraperException("Rate limited by target: " + url, ScraperException.Reason.RATE_LIMITED);
            }

            if (response.code() == 403) {
                log.warn("Forbidden (403) on URL: {} — proxy may be blocked", url);
                throw new ScraperException("Access forbidden: " + url, ScraperException.Reason.FORBIDDEN);
            }

            if (!response.isSuccessful()) {
                log.error("fetchUrl: HTTP {} for URL: {}", response.code(), url);
                throw new ScraperException(
                        "Unexpected HTTP " + response.code() + " for: " + url,
                        ScraperException.Reason.HTTP_ERROR
                );
            }

            if (response.body() == null) {
                throw new ScraperException("Empty response body for: " + url, ScraperException.Reason.EMPTY_BODY);
            }

            String body = response.body().string();
            log.debug("fetchUrl: received {} bytes from {}", body.length(), url);
            return body;

        } catch (ScraperException e) {
            throw e;  // already typed — let it propagate

        } catch (IOException e) {
            log.error("fetchUrl: network error for URL: {} — {}", url, e.getMessage());
            throw new ScraperException("Network failure for: " + url, e, ScraperException.Reason.NETWORK_ERROR);
        }
    }

    /**
     * Subclasses implement their own scraping logic.
     */
//    public abstract ScrapedPostDTO scrape();
}
