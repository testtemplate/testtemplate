package io.github.testtemplate.api;

import org.jspecify.annotations.Nullable;

public interface Variable {

  String getName();

  VariableType getType();

  @Nullable Object getValue();

  String getDescription();

  @Nullable Object getMetadata(String key);

  Object getMetadata(String key, Object defaultValue);

}
