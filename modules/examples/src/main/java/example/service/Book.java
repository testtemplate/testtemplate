package example.service;

import java.time.LocalDate;

public final class Book {

  private final String id;

  private final String title;

  private final String description;

  private final String authorId;

  private final String authorFirstName;

  private final String authorLastName;

  private final String publisher;

  private final LocalDate publishedDate;

  private final Integer pageCount;

  private Book(
      String id,
      String title,
      String description,
      String authorId,
      String authorFirstName,
      String authorLastName,
      String publisher,
      LocalDate publishedDate,
      Integer pageCount) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.authorId = authorId;
    this.authorFirstName = authorFirstName;
    this.authorLastName = authorLastName;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
    this.pageCount = pageCount;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getAuthorId() {
    return authorId;
  }

  public String getAuthorFirstName() {
    return authorFirstName;
  }

  public String getAuthorLastName() {
    return authorLastName;
  }

  public String getPublisher() {
    return publisher;
  }

  public LocalDate getPublishedDate() {
    return publishedDate;
  }

  public Integer getPageCount() {
    return pageCount;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String id;

    private String title;

    private String description;

    private String authorId;

    private String authorFirstName;

    private String authorLastName;

    private String publisher;

    private LocalDate publishedDate;

    private Integer pageCount;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder authorId(String authorId) {
      this.authorId = authorId;
      return this;
    }

    public Builder authorFirstName(String authorFirstName) {
      this.authorFirstName = authorFirstName;
      return this;
    }

    public Builder authorLastName(String authorLastName) {
      this.authorLastName = authorLastName;
      return this;
    }

    public Builder publisher(String publisher) {
      this.publisher = publisher;
      return this;
    }

    public Builder publishedDate(LocalDate publishedDate) {
      this.publishedDate = publishedDate;
      return this;
    }

    public Builder pageCount(Integer pageCount) {
      this.pageCount = pageCount;
      return this;
    }

    public Book build() {
      return new Book(
          id,
          title,
          description,
          authorId,
          authorFirstName,
          authorLastName,
          publisher,
          publishedDate,
          pageCount);
    }
  }
}
