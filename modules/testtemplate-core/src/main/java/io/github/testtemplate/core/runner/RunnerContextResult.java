package io.github.testtemplate.core.runner;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import io.github.testtemplate.api.ContextResult;

final class RunnerContextResult<R> extends AbstractRunnerContext implements ContextResult<R> {

  private final Supplier<@Nullable R> result;

  private final Supplier<Exception> exception;

  private boolean checked;

  RunnerContextResult(RunnerVariableResolver resolver, @Nullable R result) {
    super(resolver);
    this.result = () -> result;
    this.exception = () -> {
      throw new AssertionFailedError("The test expects an exception but no exception was thrown");
    };
  }

  RunnerContextResult(RunnerVariableResolver resolver, Exception exception) {
    super(resolver);
    this.exception = () -> exception;
    this.result = () -> {
      throw new AssertionFailedError("The test expects a result but an exception was thrown", exception);
    };
  }

  @Override
  public @Nullable R result() {
    checked = true;
    return result.get();
  }

  @Override
  public Throwable exception() {
    checked = true;
    return exception.get();
  }

  @Override
  public ExtensionStep with(String variable) {
    throw new TestRunnerException("Not implemented yet");
  }

  public void doubleCheck() {
    if (!checked) {
      // If the test didn't check the result or the exception, verify that there is a result and no exception
      result();
    }
  }
}
