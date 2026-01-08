package io.spring.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.data.ArticleData;
import java.util.List;
import lombok.Getter;

@Getter
public class CursorPaginatedArticleList {
  @JsonProperty("articles")
  private final List<ArticleData> articleDatas;

  @JsonProperty("articlesCount")
  private final int count;

  @JsonProperty("pageInfo")
  private final PageInfo pageInfo;

  public CursorPaginatedArticleList(
      List<ArticleData> articleDatas, int count, PageInfo pageInfo) {
    this.articleDatas = articleDatas;
    this.count = count;
    this.pageInfo = pageInfo;
  }
}
