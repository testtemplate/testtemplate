package com.github.testtemplate.core.runner;

import com.github.testtemplate.ContextView;

final class RunnerContextView extends AbstractRunnerContextView implements ContextView {

  RunnerContextView(RunnerVariableResolver variableResolver) {
    super(variableResolver);
  }
}
