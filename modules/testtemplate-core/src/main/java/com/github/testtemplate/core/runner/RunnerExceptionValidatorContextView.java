package com.github.testtemplate.core.runner;

import org.opentest4j.AssertionFailedError;

final class RunnerExceptionValidatorContextView<R> extends RunnerValidatorContextView<R> {

  private final Throwable exception;

  RunnerExceptionValidatorContextView(RunnerVariableResolver variableResolver, Throwable exception) {
    super(variableResolver);
    this.exception = exception;
  }

  @Override
  public R result() {
    throw new AssertionFailedError("The test expects a result but an exception was thrown", exception);
  }

  @Override
  public Throwable exception() {
    return exception;
  }
}
