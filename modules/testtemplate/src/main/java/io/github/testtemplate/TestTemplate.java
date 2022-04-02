package io.github.testtemplate;

import io.github.testtemplate.core.builder.TestBuilder;
import io.github.testtemplate.extension.json.JsonExtension;
import io.github.testtemplate.extension.mockito.MockExtension;
import io.github.testtemplate.junit5.JUnit5TestSuiteFactory;

import org.junit.jupiter.api.DynamicNode;

import java.util.List;

public final class TestTemplate {

  private static JsonExtension<?, ?> jsonExtension;
  private static MockExtension<?, ?> mockExtension;

  private TestTemplate() {}

  public static DefaultTestTemplateBuilder<List<DynamicNode>> defaultTest(String name) {
    return junit5().defaultTest(name);
  }

  public static TestTemplatePreBuilder<List<DynamicNode>> junit5() {
    return TestBuilder.builder(new JUnit5TestSuiteFactory());
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
