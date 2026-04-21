package io.github.testtemplate.core;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.function.ExceptionalFunction;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class TestVariable {

  private final String name;

  private final Map<String, @Nullable Object> metadata;

  private final ExceptionalFunction<ContextGiven, ?> valueSupplier;

  public TestVariable(
      String name,
      Map<String, @Nullable Object> metadata,
      ExceptionalFunction<ContextGiven, ?> valueSupplier) {
    this.name = name;
    this.metadata = Map.copyOf(metadata);
    this.valueSupplier = valueSupplier;
  }

  public String getName() {
    return name;
  }

  public Map<String, @Nullable Object> getMetadata() {
    return metadata;
  }

  public ExceptionalFunction<ContextGiven, ?> getValueSupplier() {
    return valueSupplier;
  }
}
