package io.github.testtemplate.core.runner;

import java.util.Map;

import org.jspecify.annotations.Nullable;

final class TestItemNameSubstitutor extends AbstractTestNameSubstitutor {

  private final String name;

  TestItemNameSubstitutor(String name, RunnerVariableResolver resolver, Map<String, @Nullable Object> metadata) {
    super(resolver, metadata);
    this.name = name;
  }

  public String getTestItemName() {
    return resolveName(this.name);
  }
}
