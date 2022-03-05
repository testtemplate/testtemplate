package com.github.testtemplate.core.runner;

import com.github.testtemplate.ValidatorContextView;

abstract class RunnerValidatorContextView<R> extends AbstractRunnerContextView implements ValidatorContextView<R> {

  RunnerValidatorContextView(RunnerVariableResolver variableResolver) {
    super(variableResolver);
  }
}
