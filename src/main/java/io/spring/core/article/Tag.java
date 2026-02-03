package io.spring.core.article;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a tag used to categorize articles.
 *
 * <p>Tags are simple name-based labels that can be attached to articles for categorization and
 * filtering. Tags are identified by their name, and existing tags are reused when creating new
 * articles with the same tag names.
 *
 * @see Article
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "name")
public class Tag {
  private String id;
  private String name;

  public Tag(String name) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
  }
}
