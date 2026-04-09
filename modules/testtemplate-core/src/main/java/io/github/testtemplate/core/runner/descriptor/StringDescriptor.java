package io.github.testtemplate.core.runner.descriptor;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.testtemplate.api.ValueDescriptor;

public class StringDescriptor implements ValueDescriptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(StringDescriptor.class);

  @Override
  public boolean isSupported(@Nullable Object value) {
    return value instanceof String;
  }

  @Override
  public String toString(@Nullable Object value) {
    var stringValue = (String) Objects.requireNonNull(value);

    if (stringValue.isEmpty()) {
      return "<empty string>";
    } else if (stringValue.isBlank()) {
      return "<blank string>";
    } else {
      return stringValue;
    }
  }
}
