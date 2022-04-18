package example.controller;

import example.service.Book;
import example.service.BookNotFoundException;
import example.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class BookSearchController {

  private final BookService service;

  public BookSearchController(BookService service) {
    this.service = service;
  }

  @GetMapping("/books")
  Flux<BookDto> search(@RequestParam(required = false) String text) {
    return Flux
        .defer(() -> StringUtils.hasText(text) ? service.search(text) : service.findAll())
        .map(BookSearchController::toDto);
  }

  @GetMapping("/books/{bookId}")
  Mono<BookDto> read(@PathVariable String bookId) {
    return service
        .read(bookId)
        .map(BookSearchController::toDto);
  }

  @ExceptionHandler(BookNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  void handleBookNotFoundException() {}

  private static BookDto toDto(Book book) {
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
