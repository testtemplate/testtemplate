package io.github.testtemplate.core.logger;

import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.VariableType;
import io.github.testtemplate.api.logger.TestLogSectionProvider;
import io.github.testtemplate.api.logger.TestLogSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class VariablesSectionProvider implements TestLogSectionProvider {

  @Override
  public String name() {
    return "VARIABLES";
  }

  @Override
  public List<String> lines(TestLogSnapshot snapshot) {
    Collection<? extends Variable> variables = snapshot.variables().values();
    if (variables.isEmpty()) {
      return List.of();
    }
    int length = getLongerVariableLength(variables);
    List<String> lines = new ArrayList<>();
    lines.add("Variables:");
    variables.forEach(v -> {
      String line = "  " + String.format("%-" + length + "s", v.getName())
          + (v.getType() == VariableType.MODIFIED ? " (M)" : "    ")
          + " = " + v.getDescription().indent(length + 9).trim();
      lines.add(line);
    });
    return lines;
  }

  private static int getLongerVariableLength(Collection<? extends Variable> variables) {
    return variables.stream().map(Variable::getName).map(String::length).max(Integer::compareTo).orElse(0);
  }

}
