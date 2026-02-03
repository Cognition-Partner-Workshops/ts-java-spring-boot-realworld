package io.spring.api.kafka;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelayedMessageRequest {

  @NotBlank(message = "Topic is required")
  private String topic;

  private String key;

  @NotBlank(message = "Message is required")
  private String message;

  private Long delayMs;
}
