package io.github.testtemplate.core.runner;

import io.github.testtemplate.Context;

final class RunnerContext extends AbstractRunnerContextView implements Context {

  RunnerContext(RunnerVariableResolver variableResolver) {
    super(variableResolver);
  }

  @Override
  public VariableBuilder given(String variable) {
    return new InnerVariableBuilder(variable);
  }

  final class InnerVariableBuilder implements VariableBuilder {

    private final String variable;

    InnerVariableBuilder(String variable) {
      this.variable = variable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V is(V value) {
      return (V) variableResolver.getVariableOrDefault(variable, value).getValue();
    }
  }
}
