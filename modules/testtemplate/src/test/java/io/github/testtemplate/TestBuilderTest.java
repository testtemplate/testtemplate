package io.github.testtemplate;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TestBuilderTest {

  @TestFactory
  Stream<DynamicNode> smokeTest() {
    return TestBuilder
        .defaultTest("default")
        .given("greeting").is("hello")
        .when(ctx -> ctx.get("greeting") + " " + ctx.given("name").is("Alice"))
        .then(ctx -> assertThat(ctx.result()).isEqualTo("hello Alice"))

        .test("alternative 1")
        .sameAsDefault()
        .except("name").is("Bob")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("hello Bob"))

        .test("alternative 2")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("hi Alice"))

        .suite();
  }
}
