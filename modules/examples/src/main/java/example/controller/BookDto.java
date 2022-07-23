package example.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

public class BookDto {

  @JsonProperty(access = READ_ONLY)
  private String id;

  @NotBlank
  private String title;

  @NotBlank
  private String description;

  @JsonProperty(access = WRITE_ONLY)
  private String authorId;

  @JsonProperty(access = READ_ONLY)
  private String author;

  private String publisher;

  private LocalDate publishedDate;

  @Min(0)
  private Integer pageCount;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public LocalDate getPublishedDate() {
    return publishedDate;
  }

  public void setPublishedDate(LocalDate publishedDate) {
    this.publishedDate = publishedDate;
  }

  public Integer getPageCount() {
    return pageCount;
  }

  public void setPageCount(Integer pageCount) {
    this.pageCount = pageCount;
  }
}
