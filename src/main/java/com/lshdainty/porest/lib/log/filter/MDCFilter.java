package com.lshdainty.porest.lib.log.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class MDCFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MDC.put("requestId", generateCorrelationId());
        chain.doFilter(request, response);
        MDC.clear();    // 또는 MDC.remove(REQUEST_ID);
    }

    private String generateCorrelationId() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }
}