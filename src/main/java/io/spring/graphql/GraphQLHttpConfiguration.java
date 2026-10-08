package io.spring.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.webmvc.GraphQlHttpHandler;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * The REST API configures Jackson with {@code UNWRAP_ROOT_VALUE=true} (payloads are wrapped in a
 * root element such as {@code {"user": {...}}}). The Spring GraphQL HTTP transport would otherwise
 * inherit that ObjectMapper and reject every {@code {"query": ...}} request body with 400, so the
 * {@code /graphql} endpoint gets a dedicated converter backed by a plain ObjectMapper.
 */
@Configuration
public class GraphQLHttpConfiguration {

  @Bean
  public GraphQlHttpHandler graphQlHttpHandler(WebGraphQlHandler webGraphQlHandler) {
    ObjectMapper graphQlObjectMapper = JsonMapper.builder().findAndAddModules().build();
    return new GraphQlHttpHandler(
        webGraphQlHandler, new MappingJackson2HttpMessageConverter(graphQlObjectMapper));
  }
}
