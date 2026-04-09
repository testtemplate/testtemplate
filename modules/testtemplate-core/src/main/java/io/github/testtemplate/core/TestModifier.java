package io.github.testtemplate.core;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.function.ExceptionalFunction;

public final class TestModifier {

  private final String name;

  private final Map<String, @Nullable Object> metadata;

  private final ExceptionalFunction<Context, ?> valueSupplier;

  public TestModifier(
      String name,
      Map<String, @Nullable Object> metadata,
      ExceptionalFunction<Context, ?> valueSupplier) {
    this.name = name;
    this.valueSupplier = valueSupplier;
    this.metadata = Map.copyOf(metadata);
  }

  public String getName() {
    return name;
  }

  public Map<String, @Nullable Object> getMetadata() {
    return metadata;
  }

  public ExceptionalFunction<Context, ?> getValueSupplier() {
    return valueSupplier;
  }
}
