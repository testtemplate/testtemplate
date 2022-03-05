package com.github.testtemplate.core.runner;

import com.github.testtemplate.core.TestModifier;
import com.github.testtemplate.core.TestVariable;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractRunnerContextViewTest {

  protected final RunnerVariableResolver variableResolver = new RunnerVariableResolver(
      Set.of(new TestVariable("greeting", c -> "welcome")),
      Set.of(new TestModifier("last-name", c -> "Brown")));

  abstract AbstractRunnerContextView newContextView();

  @Test
  void getShouldReturnVariableValue() {
    var value = newContextView().get("greeting");
    assertThat(value).isEqualTo("welcome");
  }
}
