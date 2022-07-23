package example.controller;

import example.service.BookService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
        .map(BookDtoConverter::toDto);
  }

  @GetMapping("/books/{bookId}")
  Mono<BookDto> read(@PathVariable String bookId) {
    return service
        .read(bookId)
        .map(BookDtoConverter::toDto);
  }
}
