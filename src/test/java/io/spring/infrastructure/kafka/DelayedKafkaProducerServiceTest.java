package io.spring.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

class DelayedKafkaProducerServiceTest {

  @Mock private KafkaSender<String, String> kafkaSender;

  @Mock private KafkaConfig kafkaConfig;

  private DelayedKafkaProducerService delayedKafkaProducerService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    delayedKafkaProducerService = new DelayedKafkaProducerService(kafkaSender, kafkaConfig);
  }

  @SuppressWarnings("unchecked")
  @Test
  void sendDelayedMessage_shouldSendAfterDelay() {
    SenderResult<String> mockResult = mock(SenderResult.class);
    RecordMetadata recordMetadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(mockResult.recordMetadata()).thenReturn(recordMetadata);
    doReturn(Flux.just(mockResult)).when(kafkaSender).send(any());
    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);

    StepVerifier.withVirtualTime(
            () ->
                delayedKafkaProducerService.sendDelayedMessage(
                    "test-topic", "test-key", "test-message", 100L))
        .expectSubscription()
        .thenAwait(Duration.ofMillis(100))
        .assertNext(
            result -> {
              assertNotNull(result);
              assertEquals(0, result.recordMetadata().partition());
            })
        .verifyComplete();
  }

  @SuppressWarnings("unchecked")
  @Test
  void sendDelayedMessage_withNullDelay_shouldUseDefaultDelay() {
    SenderResult<String> mockResult = mock(SenderResult.class);
    RecordMetadata recordMetadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(mockResult.recordMetadata()).thenReturn(recordMetadata);
    doReturn(Flux.just(mockResult)).when(kafkaSender).send(any());
    when(kafkaConfig.getDefaultDelayMs()).thenReturn(500L);

    StepVerifier.withVirtualTime(
            () ->
                delayedKafkaProducerService.sendDelayedMessage(
                    "test-topic", "test-key", "test-message", null))
        .expectSubscription()
        .thenAwait(Duration.ofMillis(500))
        .assertNext(result -> assertNotNull(result))
        .verifyComplete();
  }

  @SuppressWarnings("unchecked")
  @Test
  void sendMessage_shouldSendWithoutDelay() {
    SenderResult<String> mockResult = mock(SenderResult.class);
    RecordMetadata recordMetadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(mockResult.recordMetadata()).thenReturn(recordMetadata);
    doReturn(Flux.just(mockResult)).when(kafkaSender).send(any());
    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);

    StepVerifier.create(
            delayedKafkaProducerService.sendMessage("test-topic", "test-key", "test-message"))
        .assertNext(
            result -> {
              assertNotNull(result);
              assertEquals(0, result.recordMetadata().partition());
            })
        .verifyComplete();
  }

  @SuppressWarnings("unchecked")
  @Test
  void sendDelayedMessage_withNullKey_shouldSendSuccessfully() {
    SenderResult<String> mockResult = mock(SenderResult.class);
    RecordMetadata recordMetadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(mockResult.recordMetadata()).thenReturn(recordMetadata);
    doReturn(Flux.just(mockResult)).when(kafkaSender).send(any());
    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);

    StepVerifier.create(
            delayedKafkaProducerService.sendDelayedMessage("test-topic", null, "test-message", 0L))
        .assertNext(result -> assertNotNull(result))
        .verifyComplete();
  }
}
