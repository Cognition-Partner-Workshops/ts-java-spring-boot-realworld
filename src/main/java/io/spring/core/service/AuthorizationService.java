package io.spring.core.service;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
  public boolean canWriteArticle(User user, Article article) {
    return user.getId().equals(article.getUserId());
  }

  public boolean canWriteComment(User user, Article article, Comment comment) {
    return user.getId().equals(article.getUserId()) || user.getId().equals(comment.getUserId());
  }
}
