package io.github.testtemplate.core.runner;

class RunnerContextViewTest extends AbstractRunnerContextViewTest {

  @Override
  RunnerContextView newContextView() {
    return new RunnerContextView(this.variableResolver);
  }
}
