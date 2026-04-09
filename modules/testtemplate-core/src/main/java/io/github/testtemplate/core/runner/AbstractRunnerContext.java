package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.Context;

abstract class AbstractRunnerContext implements Context {

  protected final RunnerVariableResolver resolver;

  AbstractRunnerContext(RunnerVariableResolver resolver) {
    this.resolver = resolver;
  }

  @SuppressWarnings("unchecked")
  public <V> V get(String variable) {
    return (V) resolver.getVariable(variable).getValue();
  }
}
