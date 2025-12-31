package io.github.testtemplate.junit;

import io.github.testtemplate.TestSuiteFactory;
import io.github.testtemplate.core.builder.TestBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JUnitTestSuiteFactoryTest {

  @TestFactory
  Stream<DynamicNode> runWithJUnit() {
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
        .test("parameterized")
        .sameAsDefault()
        .except("greeting").is("good morning").or("good afternoon")
        .then(c -> assertThat(c.result()).asString().startsWith("good"));

    builder
        .test("disabled")
        .disabled()
        .sameAsDefault()
        .then(c -> assertThat(c.result()).isEqualTo("oops disabled"));

    return builder.suite();
  }

  @Test
  void shouldThrowExceptionWhenTestTypeIsUnknown() {
    var factory = new JUnitTestSuiteFactory();
    Assertions
        .assertThatThrownBy(() -> factory
            .getSuite(Stream.<TestSuiteFactory.Test>of(new UnkownTest()))
            .findFirst())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown test: io.github.testtemplate.junit.JUnitTestSuiteFactoryTest$UnkownTest");
  }

  static class UnkownTest implements TestSuiteFactory.Test {
    @Override
    public String getName() {
      return "unknown";
    }
  }
}
