package io.github.testtemplate.core;

import io.github.testtemplate.api.TestType;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class TestDefinition<R> {

  private final TestType type;

  private final String name;

  private final Map<String, @Nullable Object> metadata;

  private final List<TestVariable> variables;

  private final List<TestModifier> modifiers;

  private final List<TestParameter> parameters;

  private final TestTemplate<R> template;

  private final TestValidator<R> validator;

  public TestDefinition(
      TestType type,
      String name,
      Map<String, @Nullable Object> metadata,
      Collection<TestVariable> variables,
      Collection<TestModifier> modifiers,
      Collection<TestParameter> parameters,
      TestTemplate<R> template,
      TestValidator<R> validator) {
    this.type = type;
    this.name = name;
    this.metadata = Map.copyOf(metadata);
    this.variables = List.copyOf(variables);
    this.modifiers = List.copyOf(modifiers);
    this.parameters = List.copyOf(parameters);
    this.template = template;
    this.validator = validator;
  }

  public TestType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Map<String, @Nullable Object> getMetadata() {
    return metadata;
  }

  public List<TestVariable> getVariables() {
    return variables;
  }

  public List<TestModifier> getModifiers() {
    return modifiers;
  }

  public List<TestParameter> getParameters() {
    return parameters;
  }

  public TestTemplate<R> getTemplate() {
    return template;
  }

  public TestValidator<R> getValidator() {
    return validator;
  }
}
