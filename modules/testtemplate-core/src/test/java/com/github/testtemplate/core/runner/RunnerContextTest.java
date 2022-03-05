package com.github.testtemplate.core.runner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerContextTest extends AbstractRunnerContextViewTest {

  @Override
  RunnerContext newContextView() {
    return new RunnerContext(this.variableResolver);
  }

  @Test
  void givenShouldReturnDefaultValue() {
    var value = newContextView().given("first-name").is("Alice");
    assertThat(value).isEqualTo("Alice");
  }

  @Test
  void givenShouldReturnModifiedValue() {
    var value = newContextView().given("last-name").is("White");
    assertThat(value).isEqualTo("Brown");
  }
}
