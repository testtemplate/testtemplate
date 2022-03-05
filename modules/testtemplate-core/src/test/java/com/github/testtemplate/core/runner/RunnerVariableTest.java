package com.github.testtemplate.core.runner;

import com.github.testtemplate.TestListener;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerVariableTest {

  @Test
  void getValueShouldBeLoadedOnlyOnce() {
    RunnerVariable variable = new RunnerVariable(
        "math",
        TestListener.VariableType.ORIGINAL,
        Math::random);

    assertThat(variable).hasFieldOrPropertyWithValue("valueLoaded", false);

    var firstCall = variable.getValue();

    assertThat(variable).hasFieldOrPropertyWithValue("valueLoaded", true);

    var secondCall = variable.getValue();

    assertThat(secondCall).isSameAs(firstCall);
  }
}
