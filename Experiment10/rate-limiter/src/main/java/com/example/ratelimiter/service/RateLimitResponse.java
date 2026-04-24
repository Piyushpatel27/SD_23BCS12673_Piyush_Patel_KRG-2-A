package com.example.ratelimiter.service;

public class RateLimitResponse {

    private boolean allowed;
    private long remaining;
    private long resetTime;

    public RateLimitResponse(boolean allowed, long remaining, long resetTime) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.resetTime = resetTime;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getResetTime() {
        return resetTime;
    }
}