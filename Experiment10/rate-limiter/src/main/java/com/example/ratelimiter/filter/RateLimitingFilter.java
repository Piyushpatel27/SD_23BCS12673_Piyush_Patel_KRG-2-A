package com.example.ratelimiter.filter;

import com.example.ratelimiter.service.RateLimiterService;
import com.example.ratelimiter.service.RateLimitResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 5;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getRemoteAddr();

        RateLimitResponse rateLimit = rateLimiterService.allowRequest(userId);

        response.setHeader("X-RateLimit-Limit", String.valueOf(CAPACITY));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimit.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimit.getResetTime()));

        if (!rateLimit.isAllowed()) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
            return;
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/redis");
    }
}