package example.controller;

import io.github.testtemplate.TestTemplate;

import example.service.Book;
import example.service.BookService;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

import static io.github.testtemplate.TestTemplate.json;
import static io.github.testtemplate.TestTemplate.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.json.JsonCompareMode.STRICT;

@WebFluxTest(controllers = BookAdminController.class)
class BookAdminControllerTest {

  @MockitoBean
  private BookService service;

  @Autowired
  private WebTestClient client;

  @TestFactory
  Stream<DynamicNode> create() {
    return TestTemplate
        .defaultTest("Should create book")
        .given("service").as(mock()).use(service)
            .invoking(mock -> mock.create(Mockito.any())).willAnswer(i -> Mono.just(savedBook(i.getArgument(0))))
        .given("request-body").is(() -> """
            {
              "id": "1000",
              "title": "Greatest Book Ever",
              "description": "...",
              "authorId": "70",
              "publisher": "Imaginary Inc.",
              "publishedDate": "2022-04-18",
              "pageCount": 101
            }
            """)
        .when(ctx -> client
            .post().uri("/books")
            .contentType(APPLICATION_JSON)
            .bodyValue(ctx.get("request-body"))
            .exchange())
        .then(ctx -> ctx.result()
            .expectStatus().isCreated()
            .expectBody().json("""
                {
                  "id": "9000",
                  "title": "Greatest Book Ever",
                  "description": "...",
                  "author": "Brown, Alice",
                  "publisher": "Imaginary Inc.",
                  "publishedDate": "2022-04-18",
                  "pageCount": 101
                }
                """, STRICT))

        .test("Should return 400 bad request when title is null")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.title").is(null)
        .then(ctx -> ctx.result().expectStatus().isBadRequest())

        .test("Should return 400 bad request when title is blank")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.title").is(" ")
        .then(ctx -> ctx.result().expectStatus().isBadRequest())

        .test("Should return 400 bad request when description is null")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.description").is(null)
        .then(ctx -> ctx.result().expectStatus().isBadRequest())

        .test("Should return 400 bad request when description is empty")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.description").is(" ")
        .then(ctx -> ctx.result().expectStatus().isBadRequest())

        .test("Should ignore missing publisher")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.publisher").isAbsent()
        .then(ctx -> ctx.result().expectStatus().isCreated().expectBody().json("{\"publisher\": null}"))

        .test("Should ignore missing published date")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.publishedDate").isAbsent()
        .then(ctx -> ctx.result().expectStatus().isCreated().expectBody().json("{\"publishedDate\": null}"))

        .test("Should return 400 bad request when published date is malformed date")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.publishedDate").is("wrong")
        .then(ctx -> ctx.result().expectStatus().isBadRequest())

        .test("Should return 400 bad request when page count is negative")
        .sameAsDefault()
        .except("request-body").as(json()).path("$.pageCount").is(-12)
        .then(ctx -> ctx.result().expectStatus().isBadRequest())

        .suite();
  }

  private static Book savedBook(Book book) {
    return Book.builder()
        .id("9000")
        .title(book.getTitle())
        .description(book.getDescription())
        .authorId(book.getAuthorId())
        .authorFirstName("Alice")
        .authorLastName("Brown")
        .publisher(book.getPublisher())
        .publishedDate(book.getPublishedDate())
        .pageCount(book.getPageCount())
        .build();
  }
}
