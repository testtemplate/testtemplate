package io.github.testtemplate;

import io.github.testtemplate.core.builder.TestBuilder;
import io.github.testtemplate.extension.json.JsonExtension;
import io.github.testtemplate.extension.mockito.MockExtension;
import io.github.testtemplate.junit.JUnitTestSuiteFactory;

import org.junit.jupiter.api.DynamicNode;

import java.util.stream.Stream;

public final class TestTemplate {

  private static JsonExtension<?, ?> jsonExtension;
  private static MockExtension<?, ?> mockExtension;

  private TestTemplate() {}

  public static DefaultTestTemplateBuilder<Stream<DynamicNode>> defaultTest(String name) {
    return junit().defaultTest(name);
  }

  public static TestTemplatePreBuilder<Stream<DynamicNode>> junit() {
    return TestBuilder.builder(new JUnitTestSuiteFactory());
  }

  @SuppressWarnings("unchecked")
  public static <S, R> JsonExtension<S, R> json() {
    if (jsonExtension == null) {
      jsonExtension = new JsonExtension<>();
    }

    return (JsonExtension<S, R>) jsonExtension;
  }

  @SuppressWarnings("unchecked")
  public static <S, R> MockExtension<S, R> mock() {
    if (mockExtension == null) {
      mockExtension = new MockExtension<>();
    }

    return (MockExtension<S, R>) mockExtension;
  }
}
