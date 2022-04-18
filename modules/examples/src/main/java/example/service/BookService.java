package example.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookService {

  Mono<Book> read(String id);

  Flux<Book> findAll();

  Flux<Book> search(String text);

}
