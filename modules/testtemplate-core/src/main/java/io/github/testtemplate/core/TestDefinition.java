package io.github.testtemplate.core;

import io.github.testtemplate.ContextualTemplate;
import io.github.testtemplate.ContextualValidator;
import io.github.testtemplate.TestType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class TestDefinition<R> {

  private final String name;

  private final TestType type;

  private final ContextualTemplate<R> template;

  private final List<TestVariable> variables = new ArrayList<>();

  private final List<TestModifier> modifiers = new ArrayList<>();

  private final List<TestParameter> parameters = new ArrayList<>();

  private final ContextualValidator<R> validator;

  private final Map<String, Object> attributes = new HashMap<>();

  public TestDefinition(
      String name,
      TestType type,
      ContextualTemplate<R> template,
      Collection<TestVariable> variables,
      Collection<TestModifier> modifiers,
      Collection<TestParameter> parameters,
      ContextualValidator<R> validator,
      Map<String, Object> attributes) {
    this.name = name;
    this.type = type;
    this.template = template;
    this.variables.addAll(variables);
    this.modifiers.addAll(modifiers);
    this.parameters.addAll(parameters);
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

  public boolean isParameterized() {
    return !parameters.isEmpty();
  }

  public Stream<TestDefinition<R>> deparameterize() {
    TestParameter firstParameter = parameters.getFirst();
    return IntStream
        .range(0, parameters.size())
        .mapToObj(index -> {
          List<TestModifier> newModifiers = new ArrayList<>(modifiers);
          List<TestParameter> newParameters = new ArrayList<>(parameters);

          for (var parameter : newParameters) {
            if (parameter.getGroup().equals(firstParameter.getGroup())) {
              newModifiers.add(parameter.deparameterize(index));
            }
          }

          newParameters.removeIf(p -> p.getGroup().equals(firstParameter.getGroup()));

          return new TestDefinition<>(
              name,
              type,
              template,
              variables,
              newModifiers,
              newParameters,
              validator,
              attributes);
        });
  }

  /*

    public List<TestDefinition<R>> deparameterize() {
    var newTests = new ArrayList<TestDefinition<R>>();

    var firstParameter = parameters.getFirst();
    for (int i = 0; i < firstParameter.getValueSuppliers().size(); i++) {
      var newTest = this.copy();

      for (var parameter : parameters) {
        if (parameter.getGroup().equals(firstParameter.getGroup())) {
          newTest.withModifier(parameter.deparameterize(i));
        }
      }

      newTest.withoutParameterGroup(firstParameter.getGroup());

      newTest.withName("when " + firstParameter.getName() + " is ${" + firstParameter.getName() + "}");

      newTests.add(newTest);
    }

    return newTests;
  }


   */


  public ContextualValidator<R> getValidator() {
    return validator;
  }

  public Map<String, Object> getAttributes() {
    return unmodifiableMap(attributes);
  }
}
