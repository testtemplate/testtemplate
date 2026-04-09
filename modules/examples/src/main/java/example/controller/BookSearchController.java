package example.controller;

import example.service.BookService;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BookSearchController {

  private final BookService service;

  public BookSearchController(BookService service) {
    this.service = service;
  }

  @GetMapping("/books")
  List<BookDto> search(@RequestParam(required = false) String text) {
    if (StringUtils.hasText(text)) {
      return service.search(text).stream().map(BookDtoConverter::toDto).toList();
    } else {
      return service.findAll().stream().map(BookDtoConverter::toDto).toList();
    }
  }

  @GetMapping("/books/{bookId}")
  BookDto read(@PathVariable String bookId) {
    return BookDtoConverter.toDto(service.read(bookId));
  }
}
