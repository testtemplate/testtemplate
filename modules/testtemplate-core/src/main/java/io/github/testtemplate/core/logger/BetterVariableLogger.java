package io.github.testtemplate.core.logger;

import io.github.testtemplate.Variable;
import io.github.testtemplate.VariableLogger;

import java.util.List;

public final class BetterVariableLogger {

  private static final List<VariableLogger> LOGGERS;

  static {
    LOGGERS = List.of(
        new StringVariableLogger(),
        new ObjectVariableLogger());
  }

  private BetterVariableLogger() {
    // Utility Class
  }

  public static String toValueString(Variable variable) {
    return LOGGERS.stream()
        .filter(l -> l.isSupported(variable))
        .findFirst()
        .map(l -> l.toString(variable))
        .orElse(String.valueOf(variable.getValue()));
  }
}
