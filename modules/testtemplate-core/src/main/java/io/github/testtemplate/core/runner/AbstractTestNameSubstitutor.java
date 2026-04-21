package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.VariableType;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.testtemplate.core.runner.RunnerTestInstantiator.RUNNER_METADATA_INDEXES;
import static io.github.testtemplate.core.runner.RunnerTestInstantiator.RUNNER_METADATA_LEVEL;
import static io.github.testtemplate.core.runner.RunnerTestInstantiator.VARIABLE_METADATA_LEVEL;

abstract class AbstractTestNameSubstitutor {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(?<token>.+?)}");

  protected final RunnerVariableResolver resolver;

  protected final Map<String, @Nullable Object> metadata;

  protected AbstractTestNameSubstitutor(
      RunnerVariableResolver resolver,
      Map<String, @Nullable Object> metadata) {
    this.resolver = resolver;
    this.metadata = metadata;
  }

  protected final String resolveName(String name) {
    var sb = new StringBuilder();
    var matcher = PLACEHOLDER_PATTERN.matcher(name);
    while (matcher.find()) {
      var token = matcher.group("token");
      var value = resolveToken(token);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private String resolveToken(String token) {
    return switch (token) {
      case "index" -> resolveIndex();
      case "exceptions" -> resolveModifiers();
      case "exception-names" -> resolveModifierNames();
      case "exception-values" -> resolveModifierValues();
      default -> resolveVariable(token);
    };
  }

  private String resolveIndex() {
    Indexes indexes = (Indexes) Objects.requireNonNull(metadata.get(RUNNER_METADATA_INDEXES));
    return indexes.toString();
  }

  private List<Variable> getModifiers() {
    var level = Objects.requireNonNull(metadata.get(RUNNER_METADATA_LEVEL));

    var modifiers = new ArrayList<Variable>();
    resolver.getVariableNames().forEach(name -> {
      var variable = resolver.getVariable(name);
      if (variable.getType() == VariableType.MODIFIED) {
        var variableLevel = variable.getMetadata(VARIABLE_METADATA_LEVEL, 0);
        if (variableLevel == level) {
          modifiers.add(variable);
        }
      }
    });

    modifiers.sort((a, b) -> {
      int orderA = (int) a.getMetadata("io.github.testtemplate.variable.order", 0);
      int orderB = (int) b.getMetadata("io.github.testtemplate.variable.order", 0);
      return orderA - orderB;
    });

    return modifiers;
  }

  private String resolveModifiers() {
    var modifiers = getModifiers();

    if (modifiers.size() == 1) {
      return modifiers.getFirst().getName()
          + " is "
          + modifiers.getFirst().getDescription();
    } else if (modifiers.size() > 1) {
      return modifiers.stream().map(Variable::getName).collect(Collectors.joining(", "))
          + " are ["
          + modifiers.stream().map(Variable::getDescription).collect(Collectors.joining(", "))
          + "]";
    } else {
      return "${exceptions}";
    }
  }

  private String resolveModifierNames() {
    var modifiers = getModifiers();

    if (modifiers.size() == 1) {
      return modifiers.getFirst().getName();
    } else if (modifiers.size() > 1) {
      return modifiers.stream().map(Variable::getName).collect(Collectors.joining(", "));
    } else {
      return "${exceptions}";
    }
  }

  private String resolveModifierValues() {
    var modifiers = getModifiers();

    if (modifiers.size() == 1) {
      return modifiers.getFirst().getDescription();
    } else if (modifiers.size() > 1) {
      return modifiers.stream().map(Variable::getDescription).collect(Collectors.joining(", "));
    } else {
      return "${exceptions}";
    }
  }

  private String resolveVariable(String token) {
    try {
      return resolver.getVariable(token).getDescription();
    } catch (TestRunnerException e) {
      return "${" + token + "}";
    }
  }
}
