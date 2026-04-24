package com.example.ratelimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimiterService {

    private static final int CAPACITY = 5;
    private static final int REFILL_RATE = 1; // tokens per second

    private static final String LUA_SCRIPT = """
    local key = KEYS[1]
    local capacity = tonumber(ARGV[1])
    local refill_rate = tonumber(ARGV[2])
    local current_time = tonumber(ARGV[3])
    
    local data = redis.call("GET", key)
    
    local tokens
    local last_refill
    
    if data then
        local parts = {}
        for token in string.gmatch(data, "([^:]+)") do
            table.insert(parts, token)
        end
        tokens = tonumber(parts[1])
        last_refill = tonumber(parts[2])
    else
        tokens = capacity
        last_refill = current_time
    end
    
    local delta = math.max(0, current_time - last_refill)
    local refill = math.floor(delta * refill_rate)
    
    tokens = math.min(capacity, tokens + refill)
    
    local allowed = 0
    
    if tokens > 0 then
        tokens = tokens - 1
        allowed = 1
    end
    
    redis.call("SET", key, tokens .. ":" .. current_time, "EX", 60)
    
    local reset_time = current_time + math.ceil((capacity - tokens) / refill_rate)
    
    return {allowed, tokens, reset_time}
""";


    private final DefaultRedisScript<List> redisScript =new DefaultRedisScript<>(LUA_SCRIPT, List.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public RateLimitResponse allowRequest(String userId) {

        String key = "rate_limit:" + userId;
        long currentTime = System.currentTimeMillis() / 1000;

        List<?> result = redisTemplate.execute(
                redisScript,
                List.of(key),
                String.valueOf(CAPACITY),
                String.valueOf(REFILL_RATE),
                String.valueOf(currentTime)
        );

        if (result == null || result.size() < 3) {
            return new RateLimitResponse(true, CAPACITY, currentTime);
        }

        boolean allowed = ((Long) result.get(0)) == 1;
        long remaining = (Long) result.get(1);
        long resetTime = (Long) result.get(2);

        return new RateLimitResponse(allowed, remaining, resetTime);
    }

}


