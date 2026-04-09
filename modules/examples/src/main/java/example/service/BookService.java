package example.service;

import java.util.List;

public interface BookService {

  Book create(Book book);

  Book read(String id) throws NotFoundException;

  Book update(String id, Book book) throws NotFoundException;

  void delete(String id) throws NotFoundException;

  List<Book> findAll();

  List<Book> search(String text);

}
