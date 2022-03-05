package com.github.testtemplate.core.runner;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class RunnerExceptionValidatorContextViewTest extends AbstractRunnerContextViewTest {

  @Override
  RunnerExceptionValidatorContextView<String> newContextView() {
    return new RunnerExceptionValidatorContextView<>(this.variableResolver, new IllegalArgumentException("test"));
  }

  @Test
  void resultShouldThrowException() {
    Assertions
        .assertThatThrownBy(() -> newContextView().result())
        .isInstanceOf(AssertionFailedError.class)
        .hasMessage("The test expects a result but an exception was thrown")
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void exceptionShouldReturnCaughtException() {
    Throwable exception = newContextView().exception();

    Assertions
        .assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("test");
  }
}
