package io.spring.api.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.infrastructure.kafka.DelayedKafkaProducerService;
import io.spring.infrastructure.kafka.KafkaConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

class KafkaMessageApiTest {

  @Mock private DelayedKafkaProducerService delayedKafkaProducerService;

  @Mock private KafkaConfig kafkaConfig;

  private KafkaMessageApi kafkaMessageApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    kafkaMessageApi = new KafkaMessageApi(delayedKafkaProducerService, kafkaConfig);
  }

  @SuppressWarnings("unchecked")
  @Test
  void publishDelayedMessage_shouldReturnSuccess() {
    SenderResult<String> mockResult = mock(SenderResult.class);
    RecordMetadata recordMetadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);
    when(mockResult.recordMetadata()).thenReturn(recordMetadata);
    doReturn(Mono.just(mockResult))
        .when(delayedKafkaProducerService)
        .sendDelayedMessage(anyString(), anyString(), anyString(), any());

    DelayedMessageRequest request = new DelayedMessageRequest("test-topic", "test-key", "test-message", 1000L);

    Mono<ResponseEntity<DelayedMessageResponse>> result =
        kafkaMessageApi.publishDelayedMessage(request);

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertEquals("SUCCESS", response.getBody().getStatus());
              assertEquals("test-topic", response.getBody().getTopic());
              assertEquals("test-key", response.getBody().getKey());
              assertEquals(0, response.getBody().getPartition());
              assertEquals(0L, response.getBody().getOffset());
            })
        .verifyComplete();
  }

  @SuppressWarnings("unchecked")
  @Test
  void publishDelayedMessageAsync_shouldReturnAccepted() {
    SenderResult<String> mockResult = mock(SenderResult.class);
    RecordMetadata recordMetadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);
    when(mockResult.recordMetadata()).thenReturn(recordMetadata);
    doReturn(Mono.just(mockResult))
        .when(delayedKafkaProducerService)
        .sendDelayedMessage(anyString(), anyString(), anyString(), any());

    DelayedMessageRequest request = new DelayedMessageRequest("test-topic", "test-key", "test-message", 5000L);

    ResponseEntity<DelayedMessageResponse> response =
        kafkaMessageApi.publishDelayedMessageAsync(request);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertEquals("ACCEPTED", response.getBody().getStatus());
    assertEquals("test-topic", response.getBody().getTopic());
    assertEquals("test-key", response.getBody().getKey());
    assertEquals(5000L, response.getBody().getDelayMs());
  }

  @SuppressWarnings("unchecked")
  @Test
  void publishDelayedMessage_withDefaultDelay_shouldUseConfiguredDefault() {
    SenderResult<String> mockResult = mock(SenderResult.class);
    RecordMetadata recordMetadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(kafkaConfig.getDefaultDelayMs()).thenReturn(3000L);
    when(mockResult.recordMetadata()).thenReturn(recordMetadata);
    doReturn(Mono.just(mockResult))
        .when(delayedKafkaProducerService)
        .sendDelayedMessage(anyString(), anyString(), anyString(), any());

    DelayedMessageRequest request = new DelayedMessageRequest("test-topic", "test-key", "test-message", null);

    Mono<ResponseEntity<DelayedMessageResponse>> result =
        kafkaMessageApi.publishDelayedMessage(request);

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertEquals("SUCCESS", response.getBody().getStatus());
              assertEquals(3000L, response.getBody().getDelayMs());
            })
        .verifyComplete();
  }

  @Test
  void publishDelayedMessage_withError_shouldReturnError() {
    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);
    doReturn(Mono.error(new RuntimeException("Kafka connection failed")))
        .when(delayedKafkaProducerService)
        .sendDelayedMessage(anyString(), anyString(), anyString(), any());

    DelayedMessageRequest request = new DelayedMessageRequest("test-topic", "test-key", "test-message", 1000L);

    Mono<ResponseEntity<DelayedMessageResponse>> result =
        kafkaMessageApi.publishDelayedMessage(request);

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
              assertEquals("ERROR", response.getBody().getStatus());
              assertEquals("Kafka connection failed", response.getBody().getErrorMessage());
            })
        .verifyComplete();
  }
}
