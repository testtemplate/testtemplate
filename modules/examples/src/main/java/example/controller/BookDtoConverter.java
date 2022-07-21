package example.controller;

import example.service.Book;

final class BookDtoConverter {

  private BookDtoConverter() {}

  static Book fromDto(BookDto dto) {
    return Book.builder()
        .id(dto.getId())
        .title(dto.getTitle())
        .description(dto.getDescription())
        .authorId(dto.getAuthorId())
        .publisher(dto.getPublisher())
        .publishedDate(dto.getPublishedDate())
        .pageCount(dto.getPageCount())
        .build();
  }

  static BookDto toDto(Book book) {
    var dto = new BookDto();
    dto.setId(book.getId());
    dto.setTitle(book.getTitle());
    dto.setDescription(book.getDescription());
    dto.setAuthorId(book.getAuthorId());
    dto.setAuthor(PersonNameUtils.formatName(book.getAuthorFirstName(), book.getAuthorLastName()));
    dto.setPublisher(book.getPublisher());
    dto.setPublishedDate(book.getPublishedDate());
    dto.setPageCount(book.getPageCount());
    return dto;
  }
}
