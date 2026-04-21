package io.github.testtemplate.core.runner.descriptor;

import io.github.testtemplate.api.ValueDescriptor;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ObjectDescriptor implements ValueDescriptor {

  @Override
  public boolean isSupported(@Nullable Object value) {
    return true;
  }

  @Override
  public String toString(@Nullable Object value) {
    return Objects.toString(value, "<null>");
  }
}
