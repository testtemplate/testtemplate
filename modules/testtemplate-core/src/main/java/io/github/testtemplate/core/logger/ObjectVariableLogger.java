package io.github.testtemplate.core.logger;

import io.github.testtemplate.Variable;
import io.github.testtemplate.VariableLogger;

import java.util.Objects;

public class ObjectVariableLogger implements VariableLogger {

  @Override
  public boolean isSupported(Variable variable) {
    return true;
  }

  @Override
  public String toString(Variable variable) {
    return Objects.toString(variable.getValue(), "<null>");
  }
}
