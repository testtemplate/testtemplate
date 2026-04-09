package io.github.testtemplate.core.runner;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.ContextGiven;

final class RunnerContextGiven extends AbstractRunnerContext implements ContextGiven {

  RunnerContextGiven(RunnerVariableResolver resolver) {
    super(resolver);
  }

  @Override
  public ValueStep given(String variable) {
    return new InnerValueStep(variable);
  }

  private final class InnerValueStep implements ValueStep {

    private final String variable;

    private InnerValueStep(String variable) {
      this.variable = variable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <V> V is(V value) {
      return (V) resolver.getVariableOrDefault(variable, value).getValue();
    }
  }
}
