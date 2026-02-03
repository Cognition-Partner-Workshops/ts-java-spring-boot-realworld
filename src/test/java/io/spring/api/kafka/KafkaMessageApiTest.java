package io.spring.api.kafka;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.infrastructure.kafka.DelayedKafkaProducerService;
import io.spring.infrastructure.kafka.KafkaConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class KafkaMessageApiTest {

  @Mock private DelayedKafkaProducerService delayedKafkaProducerService;

  @Mock private KafkaConfig kafkaConfig;

  @Mock private SenderResult<String> senderResult;

  private KafkaMessageApi kafkaMessageApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    kafkaMessageApi = new KafkaMessageApi(delayedKafkaProducerService, kafkaConfig);
  }

  @Test
  void publishDelayedMessage_shouldReturnSuccess() {
    RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);
    when(senderResult.recordMetadata()).thenReturn(recordMetadata);
    when(delayedKafkaProducerService.sendDelayedMessage(anyString(), anyString(), anyString(), any()))
        .thenReturn(Mono.just(senderResult));

    given()
        .standaloneSetup(kafkaMessageApi)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(
            "{\"topic\": \"test-topic\", \"key\": \"test-key\", \"message\": \"test-message\", \"delayMs\": 1000}")
        .when()
        .post("/kafka/publish")
        .then()
        .statusCode(200)
        .body("status", equalTo("SUCCESS"))
        .body("topic", equalTo("test-topic"))
        .body("key", equalTo("test-key"))
        .body("partition", equalTo(0))
        .body("offset", equalTo(0));
  }

  @Test
  void publishDelayedMessageAsync_shouldReturnAccepted() {
    when(kafkaConfig.getDefaultDelayMs()).thenReturn(0L);
    when(delayedKafkaProducerService.sendDelayedMessage(anyString(), anyString(), anyString(), anyLong()))
        .thenReturn(Mono.empty());

    given()
        .standaloneSetup(kafkaMessageApi)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(
            "{\"topic\": \"test-topic\", \"key\": \"test-key\", \"message\": \"test-message\", \"delayMs\": 5000}")
        .when()
        .post("/kafka/publish/async")
        .then()
        .statusCode(202)
        .body("status", equalTo("ACCEPTED"))
        .body("topic", equalTo("test-topic"))
        .body("key", equalTo("test-key"))
        .body("delayMs", equalTo(5000));
  }

  @Test
  void publishDelayedMessage_withMissingTopic_shouldReturnBadRequest() {
    given()
        .standaloneSetup(kafkaMessageApi)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"key\": \"test-key\", \"message\": \"test-message\"}")
        .when()
        .post("/kafka/publish")
        .then()
        .statusCode(400);
  }

  @Test
  void publishDelayedMessage_withMissingMessage_shouldReturnBadRequest() {
    given()
        .standaloneSetup(kafkaMessageApi)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"topic\": \"test-topic\", \"key\": \"test-key\"}")
        .when()
        .post("/kafka/publish")
        .then()
        .statusCode(400);
  }

  @Test
  void publishDelayedMessage_withDefaultDelay_shouldUseConfiguredDefault() {
    RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0, 0);

    when(kafkaConfig.getDefaultDelayMs()).thenReturn(3000L);
    when(senderResult.recordMetadata()).thenReturn(recordMetadata);
    when(delayedKafkaProducerService.sendDelayedMessage(anyString(), anyString(), anyString(), any()))
        .thenReturn(Mono.just(senderResult));

    given()
        .standaloneSetup(kafkaMessageApi)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"topic\": \"test-topic\", \"key\": \"test-key\", \"message\": \"test-message\"}")
        .when()
        .post("/kafka/publish")
        .then()
        .statusCode(200)
        .body("status", equalTo("SUCCESS"))
        .body("delayMs", equalTo(3000));
  }
}
