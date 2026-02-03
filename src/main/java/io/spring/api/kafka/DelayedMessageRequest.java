package io.spring.api.kafka;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("kafkaMessage")
public class DelayedMessageRequest {

  @NotBlank(message = "Topic is required")
  private String topic;

  private String key;

  @NotBlank(message = "Message is required")
  private String message;

  private Long delayMs;
}
