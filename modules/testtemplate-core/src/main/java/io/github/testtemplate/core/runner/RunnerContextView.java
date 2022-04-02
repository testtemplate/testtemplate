package io.github.testtemplate.core.runner;

import io.github.testtemplate.ContextView;

final class RunnerContextView extends AbstractRunnerContextView implements ContextView {

  RunnerContextView(RunnerVariableResolver variableResolver) {
    super(variableResolver);
  }
}
