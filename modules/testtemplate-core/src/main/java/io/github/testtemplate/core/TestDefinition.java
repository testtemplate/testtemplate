package io.github.testtemplate.core;

import io.github.testtemplate.ContextualTemplate;
import io.github.testtemplate.ContextualValidator;
import io.github.testtemplate.TestType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class TestDefinition<R> {

  private final String name;

  private final TestType type;

  private final ContextualTemplate<R> template;

  private final List<TestVariable> variables = new ArrayList<>();

  private final List<TestModifier> modifiers = new ArrayList<>();

  private final ContextualValidator<R> validator;

  private final Map<String, Object> attributes = new HashMap<>();

  public TestDefinition(
      String name,
      TestType type,
      ContextualTemplate<R> template,
      Collection<TestVariable> variables,
      Collection<TestModifier> modifiers,
      ContextualValidator<R> validator,
      Map<String, Object> attributes) {
    this.name = name;
    this.type = type;
    this.template = template;
    this.variables.addAll(variables);
    this.modifiers.addAll(modifiers);
    this.validator = validator;
    this.attributes.putAll(attributes);
  }

  public String getName() {
    return name;
  }

  public TestType getType() {
    return type;
  }

  public ContextualTemplate<R> getTemplate() {
    return template;
  }

  public List<TestVariable> getVariables() {
    return unmodifiableList(variables);
  }

  public List<TestModifier> getModifiers() {
    return unmodifiableList(modifiers);
  }

  public ContextualValidator<R> getValidator() {
    return validator;
  }

  public Map<String, Object> getAttributes() {
    return unmodifiableMap(attributes);
  }
}
