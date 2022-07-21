package example.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookService {

  Mono<Book> create(Book book);

  Mono<Book> read(String id);

  Mono<Book> update(String id, Book book);

  void delete(String id);

  Flux<Book> findAll();

  Flux<Book> search(String text);

}
