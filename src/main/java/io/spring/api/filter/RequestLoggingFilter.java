package io.spring.api.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String CORRELATION_ID_MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String correlationId = getOrGenerateCorrelationId(request);
    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    long startTime = System.currentTimeMillis();
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String fullPath = queryString != null ? uri + "?" + queryString : uri;

    log.info("Request started: {} {}", method, fullPath);

    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      int status = response.getStatus();

      if (status >= 500) {
        log.error(
            "Request completed: {} {} - status={} duration={}ms",
            method,
            fullPath,
            status,
            duration);
      } else if (status >= 400) {
        log.warn(
            "Request completed: {} {} - status={} duration={}ms",
            method,
            fullPath,
            status,
            duration);
      } else {
        log.info(
            "Request completed: {} {} - status={} duration={}ms",
            method,
            fullPath,
            status,
            duration);
      }

      MDC.remove(CORRELATION_ID_MDC_KEY);
    }
  }

  private String getOrGenerateCorrelationId(HttpServletRequest request) {
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.trim().isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }
    return correlationId;
  }
}
