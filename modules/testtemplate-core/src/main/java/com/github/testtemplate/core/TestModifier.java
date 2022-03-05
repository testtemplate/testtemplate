package com.github.testtemplate.core;

import com.github.testtemplate.ContextView;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

public final class TestModifier {

  private final String name;

  private final Function<ContextView, ?> valueSupplier;

  private final Map<String, Object> metadata = new HashMap<>();

  public TestModifier(String name, Function<ContextView, ?> valueSupplier) {
    this(name, valueSupplier, emptyMap());
  }

  public TestModifier(String name, Function<ContextView, ?> valueSupplier, Map<String, Object> metadata) {
    this.name = name;
    this.valueSupplier = valueSupplier;
    this.metadata.putAll(metadata);
  }

  public String getName() {
    return name;
  }

  public Function<ContextView, ?> getValueSupplier() {
    return valueSupplier;
  }

  public Map<String, Object> getMetadata() {
    return unmodifiableMap(metadata);
  }
}
