package io.github.testtemplate;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

class TestBuilderTest {

  @TestFactory
  Stream<DynamicNode> smokeTest() {
    return TestBuilder
        .defaultTest("default")
        .given("greeting").is("hello")
        .when(ctx -> ctx.get("greeting") + " " + ctx.given("name").is("Alice"))
        .then(ctx -> Assertions.assertThat(ctx.result()).isEqualTo("hello Alice"))

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
