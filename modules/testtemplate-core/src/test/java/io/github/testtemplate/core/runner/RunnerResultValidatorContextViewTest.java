package io.github.testtemplate.core.runner;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerResultValidatorContextViewTest extends AbstractRunnerContextViewTest {

  @Override
  RunnerResultValidatorContextView<String> newContextView() {
    return new RunnerResultValidatorContextView<>(this.variableResolver, "hello");
  }

  @Test
  void resultShouldReturnReturnedResult() {
    var result = newContextView().result();
    assertThat(result).isEqualTo("hello");
  }

  @Test
  void exceptionShouldReturnCaughtException() {
    Assertions
        .assertThatThrownBy(() -> newContextView().exception())
        .isInstanceOf(AssertionFailedError.class)
        .hasMessage("The test expects an exception but no exception was thrown");
  }
}
