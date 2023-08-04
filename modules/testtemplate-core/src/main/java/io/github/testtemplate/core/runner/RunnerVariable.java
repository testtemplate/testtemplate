package io.github.testtemplate.core.runner;

import io.github.testtemplate.TestListener.Variable;
import io.github.testtemplate.TestListener.VariableType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

final class RunnerVariable implements Variable {

  private final String name;

  private final VariableType type;

  private final Supplier<Object> valueSupplier;

  private final Map<String, Object> metadata = new HashMap<>();

  RunnerVariable(String name, VariableType type, Supplier<Object> valueSupplier) {
    this(name, type, valueSupplier, emptyMap());
  }

  RunnerVariable(String name, VariableType type, Supplier<Object> valueSupplier, Map<String, Object> metadata) {
    this.name = name;
    this.type = type;
    this.valueSupplier = valueSupplier;
    this.metadata.putAll(metadata);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public VariableType getType() {
    return type;
  }

  @Override
  public Object getValue() {
    return valueSupplier.get();
  }

  @Override
  public Object getMetadata(String key) {
    return metadata.get(key);
  }

  @Override
  public Object getMetadata(String key, Object defaultValue) {
    return metadata.getOrDefault(key, defaultValue);
  }
}
