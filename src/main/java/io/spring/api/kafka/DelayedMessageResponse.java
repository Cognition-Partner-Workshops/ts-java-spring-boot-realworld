package io.spring.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelayedMessageResponse {

  private String status;
  private String topic;
  private String key;
  private Long delayMs;
  private String message;
  private Integer partition;
  private Long offset;
  private String errorMessage;

  public static DelayedMessageResponse success(
      String topic, String key, Long delayMs, Integer partition, Long offset) {
    return DelayedMessageResponse.builder()
        .status("SUCCESS")
        .topic(topic)
        .key(key)
        .delayMs(delayMs)
        .partition(partition)
        .offset(offset)
        .build();
  }

  public static DelayedMessageResponse accepted(String topic, String key, Long delayMs) {
    return DelayedMessageResponse.builder()
        .status("ACCEPTED")
        .topic(topic)
        .key(key)
        .delayMs(delayMs)
        .message("Message scheduled for delayed delivery")
        .build();
  }

  public static DelayedMessageResponse error(String topic, String key, String errorMessage) {
    return DelayedMessageResponse.builder()
        .status("ERROR")
        .topic(topic)
        .key(key)
        .errorMessage(errorMessage)
        .build();
  }
}
