package io.github.testtemplate.core.runner;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.TestType;
import io.github.testtemplate.api.Variable;

final class RunnerTest implements Test {

  private final String name;

  private final TestType type;

  private final RunnerVariableResolver resolver;

  private final Map<String, @Nullable Object> attributes = new HashMap<>();

  RunnerTest(String name, TestType type, RunnerVariableResolver resolver, Map<String, @Nullable Object> attributes) {
    this.name = name;
    this.type = type;
    this.resolver = resolver;
    this.attributes.putAll(attributes);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public TestType getType() {
    return type;
  }

  @Override
  public Iterable<String> getVariableNames() {
    return resolver.getVariableNames();
  }

  @Override
  public Variable getVariable(String name) {
    return resolver.getVariable(name);
  }

  @Override
  public @Nullable Object getAttribute(String key) {
    return attributes.get(key);
  }

  @Override
  public Object getAttribute(String key, Object defaultValue) {
    return attributes.getOrDefault(key, defaultValue);
  }

  @Override
  public void setAttribute(String key, @Nullable Object value) {
    attributes.put(key, value);
  }

  @Override
  public void clearAttribute(String key) {
    attributes.remove(key);
  }
}
