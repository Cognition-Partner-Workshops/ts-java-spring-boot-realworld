package io.spring.application;

import io.spring.application.data.CommentData;
import lombok.extern.slf4j.Slf4j;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<CommentData> findById(String id, User user) {
    log.info("Entering findById() with id={}", id);
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      log.info("Exiting findById()");
      return Optional.empty();
    } else {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    log.info("Exiting findById()");
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    log.info("Entering findByArticleId() with articleId={}", articleId);
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    log.info("Exiting findByArticleId()");
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    log.info("Entering findByArticleIdWithCursor() with articleId={}", articleId);
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      log.info("Exiting findByArticleIdWithCursor()");
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    log.info("Exiting findByArticleIdWithCursor()");
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }
}
