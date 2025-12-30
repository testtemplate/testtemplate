package io.github.testtemplate.core.logger;

import io.github.testtemplate.Variable;
import io.github.testtemplate.VariableLogger;

public class StringVariableLogger implements VariableLogger {

  @Override
  public boolean isSupported(Variable variable) {
    return variable.getValue() != null && variable.getValue() instanceof String;
  }

  @Override
  public String toString(Variable variable) {
    var value = (String) variable.getValue();

    if (value.isEmpty()) {
      return "<empty>";
    } else if (value.isBlank()) {
      return "<blank>";
    } else {
      return value;
    }
  }
}
