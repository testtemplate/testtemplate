package example.controller;

import example.service.Book;
import example.service.BookService;
import io.github.testtemplate.TestBuilder;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(controllers = BookSearchController.class)
@AutoConfigureRestTestClient
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

  @MockitoBean
  private BookService bookService;

  @Autowired
  private RestTestClient client;

  @TestFactory
  Stream<DynamicNode> search() {
    return TestBuilder
        .defaultTest("should return list of wanted books")
//        .disabled()
        .given("service").as(TestBuilder.mock()).use(bookService)
        .invoking(mock -> mock.findAll()).willReturn(List.of(BOOK_1000, BOOK_2000, BOOK_3000))
        .invoking(mock -> mock.search(any())).willReturn(List.of())
        .invoking(mock -> mock.search("great")).willReturn(List.of(BOOK_1000, BOOK_2000))
        .when(ctx -> client
            .get()
            .uri(u -> u.path("/books").query(ctx.given("request-query").is("text=great")).build())
            .exchange())
        .then(ctx -> ctx.result()
            .expectStatus().isOk()
            .expectBody().json("""
                [
                  {"id": "1000", "title": "Greatest Book Ever"},
                  {"id": "2000", "title": "Great Antonio"}
                ]
                """))

        .test("should return no books when no books match the request")
        .disabled("flagueda")
        .sameAsDefault()
        .except("request-query").is("text=potato")
        .then(ctx -> ctx.result()
            .expectStatus().isOk()
            .expectBody().json("[]"))

        .test("should return all books when there is no search criteria")
        .disabled()
        .sameAsDefault()
        .except("request-query").isNull().or(" ").or("text=")
        .then(ctx -> ctx.result()
            .expectStatus().isOk()
            .expectBody().json("""
                [
                  {"id": "1000", "title": "Greatest Book Ever"},
                  {"id": "2000", "title": "Great Antonio"},
                  {"id": "3000", "title": "House of Future"}
                ]
                """))

        .suite();
  }
}
