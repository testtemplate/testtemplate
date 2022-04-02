package io.github.testtemplate.core.runner;

import io.github.testtemplate.ContextView;

abstract class AbstractRunnerContextView implements ContextView {

  protected final RunnerVariableResolver variableResolver;

  AbstractRunnerContextView(RunnerVariableResolver variableResolver) {
    this.variableResolver = variableResolver;
  }

  @SuppressWarnings("unchecked")
  public <V> V get(String variable) {
    return (V) variableResolver.getVariable(variable).getValue();
  }
}
