package io.spring.infrastructure.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaConfig {

  @Value("${kafka.bootstrap-servers:localhost:9092}")
  private String bootstrapServers;

  @Value("${kafka.producer.client-id:delayed-message-producer}")
  private String clientId;

  @Value("${kafka.default-delay-ms:0}")
  private long defaultDelayMs;

  @Bean
  public SenderOptions<String, String> senderOptions() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 3);
    return SenderOptions.create(props);
  }

  @Bean
  public KafkaSender<String, String> kafkaSender(SenderOptions<String, String> senderOptions) {
    return KafkaSender.create(senderOptions);
  }

  public long getDefaultDelayMs() {
    return defaultDelayMs;
  }
}
