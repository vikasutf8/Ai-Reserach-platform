package com.springai.airesearchagent.scraper.exception;

public class ScraperException extends RuntimeException {

    public enum Reason {
        RATE_LIMITED,   // 429 — back off and retry
        FORBIDDEN,      // 403 — proxy blocked or bot detected
        HTTP_ERROR,     // other non-2xx
        EMPTY_BODY,     // 2xx but no content
        NETWORK_ERROR   // IOException — timeout, DNS, proxy down
    }

    private final Reason reason;

    public ScraperException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public ScraperException(String message, Throwable cause, Reason reason) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}