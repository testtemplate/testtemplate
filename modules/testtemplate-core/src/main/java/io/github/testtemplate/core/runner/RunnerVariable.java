package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.VariableType;
import io.github.testtemplate.api.function.ExceptionalSupplier;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class RunnerVariable implements Variable {

  private final String name;

  private final VariableType type;

  private final ExceptionalSupplier<Object> valueSupplier;

  private final RunnerVariableDescriptor valueDescriptor;

  private final Map<String, Object> metadata = new HashMap<>();

  RunnerVariable(
      String name,
      VariableType type,
      ExceptionalSupplier<Object> valueSupplier,
      RunnerVariableDescriptor valueDescriptor) {
    this.name = name;
    this.type = type;
    this.valueSupplier = valueSupplier;
    this.valueDescriptor = valueDescriptor;
  }

  RunnerVariable(
      String name,
      VariableType type,
      ExceptionalSupplier<Object> valueSupplier,
      RunnerVariableDescriptor valueDescriptor,
      Map<String, Object> metadata) {
    this.name = name;
    this.type = type;
    this.valueSupplier = valueSupplier;
    this.valueDescriptor = valueDescriptor;
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
  public @Nullable Object getValue() {
    try {
      return valueSupplier.get();
    } catch (Exception e) {
      throw new TestRunnerException("The variable '" + name + "' has thrown an exception", e);
    }
  }

  @Override
  public String getDescription() {
    return valueDescriptor.describe(getValue());
  }

  @Override
  public @Nullable Object getMetadata(String key) {
    return metadata.get(key);
  }

  @Override
  public Object getMetadata(String key, Object defaultValue) {
    return metadata.getOrDefault(key, defaultValue);
  }
}
