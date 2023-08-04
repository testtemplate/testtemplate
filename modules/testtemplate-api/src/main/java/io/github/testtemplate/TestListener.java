package io.github.testtemplate;

public interface TestListener {

  default void before(Test test) {}

  default void after(Test test) {}

  default void result(Test test, Object result) {}

  default void exception(Test test, Throwable exception) {}

  default void variable(Test test, String name, VariableType type, Object value) {}

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

  enum VariableType {
    ORIGINAL,
    MODIFIED
  }

  interface Variable {

    String getName();

    VariableType getType();

    Object getValue();

    Object getMetadata(String key);

    Object getMetadata(String key, Object defaultValue);
  }
}
