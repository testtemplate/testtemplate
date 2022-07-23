package example.controller;

import io.github.testtemplate.TestTemplate;

import example.service.Book;
import example.service.BookService;
import example.service.NotFoundException;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static io.github.testtemplate.TestTemplate.mock;
import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(controllers = BookSearchController.class)
class BookSearchControllerTest {

  private static final Book BOOK_1000 = Book.builder()
      .id("1000").title("Greatest Book Ever").description("...")
      .authorId("70").authorFirstName("Alice").authorLastName("Brown")
      .publisher("Imaginary Inc.").publishedDate(LocalDate.of(2022, Month.APRIL, 18))
      .pageCount(101)
      .build();

  private static final Book BOOK_2000 = Book.builder()
      .id("2000").title("Great Antonio").description("...")
      .authorId("73").authorFirstName("Bobby").authorLastName("Tyler")
      .publisher("World Unicorn Inc.").publishedDate(LocalDate.of(2012, Month.MARCH, 5))
      .pageCount(248)
      .build();

  private static final Book BOOK_3000 = Book.builder()
      .id("3000").title("House of Future").description("...")
      .authorId("75").authorFirstName("Bob").authorLastName("Builder")
      .publisher("Build Lte.").publishedDate(LocalDate.of(2002, Month.OCTOBER, 9))
      .pageCount(32)
      .build();

  @MockBean
  private BookService bookService;

  @Autowired
  private WebTestClient client;

  @TestFactory
  List<DynamicNode> search() {
    return TestTemplate
        .defaultTest("should return a list of wanted books")
        .given("service").as(mock()).use(bookService)
            .invoking(mock -> mock.findAll()).willReturn(Flux.just(BOOK_1000, BOOK_2000, BOOK_3000))
            .invoking(mock -> mock.search(any())).willReturn(Flux.empty())
            .invoking(mock -> mock.search("great")).willReturn(Flux.just(BOOK_1000, BOOK_2000))
        .when(ctx -> client
            .get()
            .uri(u -> u.path("/books").query(ctx.given("request-query").is("text=great")).build())
            .exchange())
        .then(ctx -> ctx.result()
            .expectStatus().isOk()
            .expectBody().json("["
                + "{\"id\": \"1000\", \"title\": \"Greatest Book Ever\"},"
                + "{\"id\": \"2000\", \"title\": \"Great Antonio\"}"
                + "]"))

        .test("should return no books when no books match the request")
        .sameAsDefault()
        .except("request-query").is("text=potato")
        .then(ctx -> ctx.result()
            .expectStatus().isOk()
            .expectBody().json("[]"))

        .test("should return all books when there is no search criteria")
        .sameAsDefault()
        .except("request-query").isNull()
        .then(ctx -> ctx.result()
            .expectStatus().isOk()
            .expectBody().json("["
                + "{\"id\": \"1000\", \"title\": \"Greatest Book Ever\"},"
                + "{\"id\": \"2000\", \"title\": \"Great Antonio\"},"
                + "{\"id\": \"3000\", \"title\": \"House of Future\"}"
                + "]"))

        .suite();
  }

  @TestFactory
  List<DynamicNode> read() {
    return TestTemplate
        .defaultTest("should return a book")
        .given("service").as(mock()).use(bookService)
            .invoking(mock -> mock.read(any())).willThrow(NotFoundException::new)
            .invoking(mock -> mock.read("1000")).willReturn(Mono.just(BOOK_1000))
        .when(ctx -> client.get().uri("/books/{id}", ctx.given("requested-book-id").is(BOOK_1000.getId())).exchange())
        .then(ctx -> ctx.result()
            .expectStatus().isOk()
            .expectBody().json("{"
                + "\"id\": \"1000\","
                + "\"title\": \"Greatest Book Ever\","
                + "\"description\": \"...\","
                + "\"author\": \"Brown, Alice\","
                + "\"publisher\": \"Imaginary Inc.\","
                + "\"publishedDate\": \"2022-04-18\","
                + "\"pageCount\": 101}", true))

        .test("should return 404 not found when the book doesn't exist")
        .sameAsDefault()
        .except("requested-book-id").is("9999")
        .then(ctx -> ctx.result().expectStatus().isNotFound())

        .suite();
  }
}
