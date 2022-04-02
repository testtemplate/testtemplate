package io.github.testtemplate.core;

import io.github.testtemplate.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

public final class TestVariable {

  private final String name;

  private final Function<Context, ?> valueSupplier;

  private final Map<String, Object> metadata = new HashMap<>();

  public TestVariable(String name, Function<Context, ?> valueSupplier) {
    this(name, valueSupplier, emptyMap());
  }

  public TestVariable(String name, Function<Context, ?> valueSupplier, Map<String, Object> metadata) {
    this.name = name;
    this.valueSupplier = valueSupplier;
    this.metadata.putAll(metadata);
  }

  public String getName() {
    return name;
  }

  public Function<Context, ?> getValueSupplier() {
    return valueSupplier;
  }

  public Map<String, Object> getMetadata() {
    return unmodifiableMap(metadata);
  }
}
