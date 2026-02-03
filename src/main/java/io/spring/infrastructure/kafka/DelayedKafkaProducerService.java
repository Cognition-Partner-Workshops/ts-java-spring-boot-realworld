package io.spring.infrastructure.kafka;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

@Service
@Slf4j
public class DelayedKafkaProducerService {

  private final KafkaSender<String, String> kafkaSender;
  private final KafkaConfig kafkaConfig;

  public DelayedKafkaProducerService(
      KafkaSender<String, String> kafkaSender, KafkaConfig kafkaConfig) {
    this.kafkaSender = kafkaSender;
    this.kafkaConfig = kafkaConfig;
  }

  public Mono<SenderResult<String>> sendDelayedMessage(
      String topic, String key, String message, Long delayMs) {
    long effectiveDelay = delayMs != null ? delayMs : kafkaConfig.getDefaultDelayMs();

    log.info(
        "Scheduling message to topic '{}' with key '{}' after {}ms delay",
        topic,
        key,
        effectiveDelay);

    ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, message);
    SenderRecord<String, String, String> senderRecord =
        SenderRecord.create(producerRecord, key != null ? key : "");

    return Mono.delay(Duration.ofMillis(effectiveDelay))
        .doOnNext(
            tick ->
                log.info(
                    "Delay completed, sending message to topic '{}' with key '{}'", topic, key))
        .flatMap(
            tick ->
                kafkaSender
                    .send(Mono.just(senderRecord))
                    .next()
                    .doOnSuccess(
                        result ->
                            log.info(
                                "Message sent successfully to topic '{}', partition: {}, offset: {}",
                                topic,
                                result.recordMetadata().partition(),
                                result.recordMetadata().offset()))
                    .doOnError(
                        error ->
                            log.error(
                                "Failed to send message to topic '{}': {}",
                                topic,
                                error.getMessage())));
  }

  public Mono<SenderResult<String>> sendMessage(String topic, String key, String message) {
    return sendDelayedMessage(topic, key, message, 0L);
  }
}
