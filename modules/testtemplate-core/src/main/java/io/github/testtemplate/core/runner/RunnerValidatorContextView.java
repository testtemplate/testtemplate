package io.github.testtemplate.core.runner;

import io.github.testtemplate.ValidatorContextView;

abstract class RunnerValidatorContextView<R> extends AbstractRunnerContextView implements ValidatorContextView<R> {

  RunnerValidatorContextView(RunnerVariableResolver variableResolver) {
    super(variableResolver);
  }
}
