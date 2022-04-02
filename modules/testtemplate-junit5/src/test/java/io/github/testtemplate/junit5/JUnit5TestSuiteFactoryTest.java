package io.github.testtemplate.junit5;

import io.github.testtemplate.core.builder.TestBuilder;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JUnit5TestSuiteFactoryTest {

  @TestFactory
  List<DynamicNode> runWithJUnit5() {
    var builder = TestBuilder.builder(new JUnit5TestSuiteFactory())
        .defaultTest("default")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    builder
        .test("alternative")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(c -> assertThat(c.result()).isEqualTo("hi"));

    builder
        .test("disabled")
        .disabled()
        .sameAsDefault()
        .then(c -> assertThat(c.result()).isEqualTo("oops disabled"));

    return builder.suite();
  }
}
