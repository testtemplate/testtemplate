package io.github.testtemplate;

import java.util.Map;

public interface TestListener {

  default void before(Test test) {}

  default void after(Test test) {}

  default void result(Test test, Object result) {}

  default void exception(Test test, Throwable exception) {}

  default void variable(Test test, String name, VariableType type, Object value, Map<String, Object> metadata) {}

  interface Test {

    String getName();

    TestType getType();

    Iterable<String> getVariableNames();

    Variable getVariable(String name);

    Object getAttribute(String key);

    Object getAttribute(String key, Object defaultValue);

    void setAttribute(String key, Object value);

    void clearAttribute(String key);
  }
}
