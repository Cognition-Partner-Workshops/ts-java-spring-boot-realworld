package io.spring.api.logging;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
  private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
  private static final String START_TIME_ATTRIBUTE = "requestStartTime";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

    String clientIp = getClientIp(request);
    String userAgent = request.getHeader("User-Agent");

    log.info(
        "Incoming request: {} {} from IP: {} User-Agent: {}",
        request.getMethod(),
        request.getRequestURI(),
        clientIp,
        userAgent != null ? userAgent : "unknown");

    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
    long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

    String logMessage =
        String.format(
            "Completed request: %s %s - Status: %d - Duration: %dms",
            request.getMethod(), request.getRequestURI(), response.getStatus(), duration);

    if (ex != null) {
      log.error("{} - Exception: {}", logMessage, ex.getMessage());
    } else if (response.getStatus() >= 500) {
      log.error(logMessage);
    } else if (response.getStatus() >= 400) {
      log.warn(logMessage);
    } else {
      log.info(logMessage);
    }
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
}
