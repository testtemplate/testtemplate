package io.github.testtemplate;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestTemplateTest {

  @TestFactory
  List<DynamicNode> smokeTest() {
    return TestTemplate
        .defaultTest("default")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting") + " " + c.given("name").is("Alice"))
        .then(c -> assertThat(c.result()).isEqualTo("hello Alice"))

        .test("alt 1")
        .sameAsDefault()
        .except("name").is("Bob")
        .then(c -> assertThat(c.result()).isEqualTo("hello Bob"))

        .test("alt 2")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(c -> assertThat(c.result()).isEqualTo("hi Alice"))

        .suite();
  }
}
