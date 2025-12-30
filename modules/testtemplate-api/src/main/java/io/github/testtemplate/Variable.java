package io.github.testtemplate;

public interface Variable {

  String getName();

  VariableType getType();

  Object getValue();

  Object getMetadata(String key);

  Object getMetadata(String key, Object defaultValue);
}
