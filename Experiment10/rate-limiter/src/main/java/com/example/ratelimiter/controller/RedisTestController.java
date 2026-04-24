package com.example.ratelimiter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/set")
    public String setValue() {
        redisTemplate.opsForValue().set("testKey", "hello-from-spring");
        return "Value set";
    }

    @GetMapping("/get")
    public Object getValue() {
        return redisTemplate.opsForValue().get("testKey");
    }
}