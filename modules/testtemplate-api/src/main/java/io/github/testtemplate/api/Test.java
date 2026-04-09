package io.github.testtemplate.api;

import org.jspecify.annotations.Nullable;

public interface Test {

  String getName();

  TestType getType();

  Iterable<String> getVariableNames();

  Variable getVariable(String name);

  @Nullable Object getAttribute(String key);

  Object getAttribute(String key, Object defaultValue);

  void setAttribute(String key, @Nullable Object value);

  void clearAttribute(String key);

}
