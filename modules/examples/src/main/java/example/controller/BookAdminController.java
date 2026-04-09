package example.controller;

import example.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class BookAdminController {
  private final BookService service;

  public BookAdminController(BookService service) {
    this.service = service;
  }

  @PostMapping("/books")
  @ResponseStatus(CREATED)
  public BookDto create(@RequestBody @Valid BookDto dto) {
    return BookDtoConverter.toDto(service.create(BookDtoConverter.fromDto(dto)));
  }
}
