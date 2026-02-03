package io.spring.api.security;

import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class JwtTokenFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);

  @Autowired private UserRepository userRepository;
  @Autowired private JwtService jwtService;
  private final String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> tokenOpt = getTokenString(request.getHeader(header));

    if (tokenOpt.isPresent()) {
      String token = tokenOpt.get();
      Optional<String> subOpt = jwtService.getSubFromToken(token);

      if (subOpt.isPresent()) {
        String id = subOpt.get();
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
          Optional<io.spring.core.user.User> userOpt = userRepository.findById(id);
          if (userOpt.isPresent()) {
            io.spring.core.user.User user = userOpt.get();
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug(
                "User authenticated: userId={} username={} path={}",
                user.getId(),
                user.getUsername(),
                request.getRequestURI());
          } else {
            log.warn(
                "JWT token valid but user not found: userId={} path={}",
                id,
                request.getRequestURI());
          }
        }
      } else {
        log.debug("Invalid or expired JWT token for path={}", request.getRequestURI());
      }
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenString(String header) {
    if (header == null) {
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
