package io.github.testtemplate.core;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.function.ExceptionalFunction;

public class TestParameter {

  private final String name;

  private final String group;

  private final Map<String, @Nullable Object> metadata;

  private final List<ExceptionalFunction<Context, ?>> valueSuppliers;

  public TestParameter(
      String name,
      String group,
      Map<String, @Nullable Object> metadata,
      List<ExceptionalFunction<Context, ?>> valueSuppliers) {
    this.name = name;
    this.group = group;
    this.metadata = Map.copyOf(metadata);
    this.valueSuppliers = valueSuppliers;
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return group;
  }

  public Map<String, @Nullable Object> getMetadata() {
    return metadata;
  }

  public List<ExceptionalFunction<Context, ?>> getValueSuppliers() {
    return valueSuppliers;
  }
}
