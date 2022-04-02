package io.github.testtemplate.core.runner;

import io.github.testtemplate.TestListener.Test;
import io.github.testtemplate.TestListener.Variable;
import io.github.testtemplate.TestType;

import java.util.HashMap;
import java.util.Map;

final class RunnerTest implements Test {

  private final String name;

  private final TestType type;

  private final RunnerVariableResolver variableResolver;

  private final Map<String, Object> attributes = new HashMap<>();

  RunnerTest(String name, TestType type, RunnerVariableResolver variableResolver, Map<String, Object> attributes) {
    this.name = name;
    this.type = type;
    this.variableResolver = variableResolver;
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
    return variableResolver.getVariableNames();
  }

  @Override
  public Variable getVariable(String name) {
    return variableResolver.getVariable(name);
  }

  @Override
  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  @Override
  public Object getAttribute(String key, Object defaultValue) {
    return attributes.getOrDefault(key, defaultValue);
  }

  @Override
  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  @Override
  public void clearAttribute(String key) {
    attributes.remove(key);
  }
}
