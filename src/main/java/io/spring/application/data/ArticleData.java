package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.DateTimeCursor;
import io.spring.application.article.ReadingTimeCalculator;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleData implements io.spring.application.Node {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private boolean favorited;
  private int favoritesCount;
  private Instant createdAt;
  private Instant updatedAt;
  private List<String> tagList;

  @JsonProperty("author")
  private ProfileData profileData;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public int getReadingTimeMinutes() {
    return ReadingTimeCalculator.minutesFor(body);
  }

  @Override
  public DateTimeCursor getCursor() {
    return new DateTimeCursor(updatedAt);
  }
}
