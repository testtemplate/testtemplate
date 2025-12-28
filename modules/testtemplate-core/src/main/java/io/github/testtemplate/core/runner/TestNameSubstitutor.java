package io.github.testtemplate.core.runner;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TestNameSubstitutor {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(?<token>.+)}");

  private TestNameSubstitutor() {
    // Utility Class
  }

  static String resolveName(String name, RunnerVariableResolver variableResolver) {
    var sb = new StringBuilder();
    var matcher = PLACEHOLDER_PATTERN.matcher(name);
    while (matcher.find()) {
      var token = matcher.group("token");
      var value = variableResolver.getVariable(token).getValue();
      matcher.appendReplacement(sb, Matcher.quoteReplacement(Objects.toString(value)));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
