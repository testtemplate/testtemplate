package io.github.testtemplate.core.runner;

import org.jspecify.annotations.Nullable;

import java.util.Map;

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
