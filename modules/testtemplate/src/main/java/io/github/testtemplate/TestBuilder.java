package io.github.testtemplate;

import io.github.testtemplate.api.builder.DefaultBuilder;
import io.github.testtemplate.api.config.TestConfiguration;
import io.github.testtemplate.api.config.TestConfigurationLoader;
import io.github.testtemplate.core.builder.CoreBuilder;
import io.github.testtemplate.core.runner.RunnerTestInstantiator;
import io.github.testtemplate.extension.json.JsonExtension;
import io.github.testtemplate.extension.mockito.MockitoExtension;
import io.github.testtemplate.junit.JUnitTestSuiteFactory;
import org.junit.jupiter.api.DynamicNode;

import java.util.stream.Stream;

public final class TestBuilder {

  private static final TestConfiguration CONFIGURATION = TestConfigurationLoader.load();

  private TestBuilder() {}

  public static DefaultBuilder.TestMetadataStep<Stream<DynamicNode>> defaultTest(String name) {
    return junit().defaultTest(name);
  }

  public static DefaultBuilder<Stream<DynamicNode>> junit() {
    var factory = new JUnitTestSuiteFactory();
    var instantiator = instantiator();
    return CoreBuilder.builder(factory, instantiator);
  }

  public static <S, R> JsonExtension<S, R> json() {
    return JsonExtension.json();
  }

  public static <S, R> MockitoExtension<S, R> mock() {
    return MockitoExtension.mock();
  }

  private static RunnerTestInstantiator instantiator() {
    // TODO Add class singleton instance
    return new RunnerTestInstantiator(CONFIGURATION);
  }
}
