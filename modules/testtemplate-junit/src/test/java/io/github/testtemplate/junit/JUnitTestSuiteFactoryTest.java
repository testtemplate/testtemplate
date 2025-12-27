package io.github.testtemplate.junit;

import io.github.testtemplate.core.builder.TestBuilder;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JUnitTestSuiteFactoryTest {

  @TestFactory
  Stream<DynamicNode> runWithJUnit5() {
    var builder = TestBuilder.builder(new JUnitTestSuiteFactory())
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
