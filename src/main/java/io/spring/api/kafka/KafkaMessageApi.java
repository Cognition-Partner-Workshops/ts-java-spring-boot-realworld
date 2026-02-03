package io.spring.api.kafka;

import io.spring.infrastructure.kafka.DelayedKafkaProducerService;
import io.spring.infrastructure.kafka.KafkaConfig;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/kafka")
@Slf4j
public class KafkaMessageApi {

  private final DelayedKafkaProducerService delayedKafkaProducerService;
  private final KafkaConfig kafkaConfig;

  public KafkaMessageApi(
      DelayedKafkaProducerService delayedKafkaProducerService, KafkaConfig kafkaConfig) {
    this.delayedKafkaProducerService = delayedKafkaProducerService;
    this.kafkaConfig = kafkaConfig;
  }

  @PostMapping("/publish")
  public Mono<ResponseEntity<DelayedMessageResponse>> publishDelayedMessage(
      @Valid @RequestBody DelayedMessageRequest request) {

    Long effectiveDelay =
        request.getDelayMs() != null ? request.getDelayMs() : kafkaConfig.getDefaultDelayMs();

    log.info(
        "Received request to publish message to topic '{}' with delay {}ms",
        request.getTopic(),
        effectiveDelay);

    return delayedKafkaProducerService
        .sendDelayedMessage(
            request.getTopic(), request.getKey(), request.getMessage(), request.getDelayMs())
        .map(
            result ->
                ResponseEntity.ok(
                    DelayedMessageResponse.success(
                        request.getTopic(),
                        request.getKey(),
                        effectiveDelay,
                        result.recordMetadata().partition(),
                        result.recordMetadata().offset())))
        .onErrorResume(
            error -> {
              log.error("Error publishing message: {}", error.getMessage());
              return Mono.just(
                  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .body(
                          DelayedMessageResponse.error(
                              request.getTopic(), request.getKey(), error.getMessage())));
            });
  }

  @PostMapping("/publish/async")
  public ResponseEntity<DelayedMessageResponse> publishDelayedMessageAsync(
      @Valid @RequestBody DelayedMessageRequest request) {

    Long effectiveDelay =
        request.getDelayMs() != null ? request.getDelayMs() : kafkaConfig.getDefaultDelayMs();

    log.info(
        "Received async request to publish message to topic '{}' with delay {}ms",
        request.getTopic(),
        effectiveDelay);

    delayedKafkaProducerService
        .sendDelayedMessage(
            request.getTopic(), request.getKey(), request.getMessage(), request.getDelayMs())
        .subscribe(
            result ->
                log.info(
                    "Async message sent successfully to partition {} at offset {}",
                    result.recordMetadata().partition(),
                    result.recordMetadata().offset()),
            error -> log.error("Async message failed: {}", error.getMessage()));

    return ResponseEntity.accepted()
        .body(DelayedMessageResponse.accepted(request.getTopic(), request.getKey(), effectiveDelay));
  }
}
