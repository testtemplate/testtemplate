package io.github.testtemplate.core;

import io.github.testtemplate.ContextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

public final class TestParameter {

  private final String name;

  private final List<Function<ContextView, ?>> valueSuppliers = new ArrayList<>();

  private final Map<String, Object> metadata = new HashMap<>();

  public TestParameter(
      String name,
      List<Function<ContextView, ?>> valueSuppliers) {
    this(name, valueSuppliers, emptyMap());
  }

  public TestParameter(
      String name,
      List<Function<ContextView, ?>> valueSuppliers,
      Map<String, Object> metadata) {
    this.name = name;
    this.valueSuppliers.addAll(valueSuppliers);
    this.metadata.putAll(metadata);
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return name;
  }

  public List<Function<ContextView, ?>> getValueSuppliers() {
    return valueSuppliers;
  }

  public Map<String, Object> getMetadata() {
    return unmodifiableMap(metadata);
  }

  public TestModifier deparameterize(int index) {
    return new TestModifier(name, valueSuppliers.get(index), metadata);
  }
}
