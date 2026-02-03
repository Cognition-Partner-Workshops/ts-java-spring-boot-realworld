package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  public void should_create_with_builder() {
    NewArticleParam param = NewArticleParam.builder()
        .title("Test Title")
        .description("Test Description")
        .body("Test Body")
        .tagList(Arrays.asList("java", "spring"))
        .build();
    
    assertThat(param.getTitle(), is("Test Title"));
    assertThat(param.getDescription(), is("Test Description"));
    assertThat(param.getBody(), is("Test Body"));
    assertThat(param.getTagList().size(), is(2));
  }

  @Test
  public void should_create_with_all_args_constructor() {
    List<String> tags = Arrays.asList("java", "spring");
    NewArticleParam param = new NewArticleParam("Title", "Desc", "Body", tags);
    
    assertThat(param.getTitle(), is("Title"));
    assertThat(param.getDescription(), is("Desc"));
    assertThat(param.getBody(), is("Body"));
    assertThat(param.getTagList(), is(tags));
  }

  @Test
  public void should_create_with_no_args_constructor() {
    NewArticleParam param = new NewArticleParam();
    assertThat(param, is(notNullValue()));
  }

  @Test
  public void should_create_builder() {
    NewArticleParam.NewArticleParamBuilder builder = NewArticleParam.builder();
    assertThat(builder, is(notNullValue()));
  }

  @Test
  public void should_build_with_title_only() {
    NewArticleParam param = NewArticleParam.builder()
        .title("Title")
        .build();
    
    assertThat(param.getTitle(), is("Title"));
  }

  @Test
  public void should_build_with_description_only() {
    NewArticleParam param = NewArticleParam.builder()
        .description("Description")
        .build();
    
    assertThat(param.getDescription(), is("Description"));
  }

  @Test
  public void should_build_with_body_only() {
    NewArticleParam param = NewArticleParam.builder()
        .body("Body")
        .build();
    
    assertThat(param.getBody(), is("Body"));
  }

  @Test
  public void should_build_with_tag_list_only() {
    List<String> tags = Arrays.asList("java");
    NewArticleParam param = NewArticleParam.builder()
        .tagList(tags)
        .build();
    
    assertThat(param.getTagList(), is(tags));
  }

  @Test
  public void should_have_builder_to_string() {
    NewArticleParam.NewArticleParamBuilder builder = NewArticleParam.builder()
        .title("Title")
        .description("Desc")
        .body("Body");
    
    String toString = builder.toString();
    assertThat(toString, is(notNullValue()));
  }
}
