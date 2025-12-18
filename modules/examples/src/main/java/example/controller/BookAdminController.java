package example.controller;

import example.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static example.controller.BookDtoConverter.fromDto;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class BookAdminController {

  private final BookService service;

  public BookAdminController(BookService service) {
    this.service = service;
  }

  @PostMapping("/books")
  @ResponseStatus(CREATED)
  public Mono<BookDto> create(@RequestBody @Valid BookDto dto) {
    return service
        .create(fromDto(dto))
        .map(BookDtoConverter::toDto);
  }
}
