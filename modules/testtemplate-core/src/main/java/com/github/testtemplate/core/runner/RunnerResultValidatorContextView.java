package com.github.testtemplate.core.runner;

import org.opentest4j.AssertionFailedError;

final class RunnerResultValidatorContextView<R> extends RunnerValidatorContextView<R> {

  private final R result;

  RunnerResultValidatorContextView(RunnerVariableResolver variableResolver, R result) {
    super(variableResolver);
    this.result = result;
  }

  @Override
  public R result() {
    return result;
  }

  @Override
  public Throwable exception() {
    throw new AssertionFailedError("The test expects an exception but no exception was thrown");
  }
}
