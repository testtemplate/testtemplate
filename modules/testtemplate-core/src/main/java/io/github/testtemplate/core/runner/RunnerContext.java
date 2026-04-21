package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.Context;

final class RunnerContext extends AbstractRunnerContext implements Context {

  RunnerContext(RunnerVariableResolver resolver) {
    super(resolver);
  }
}
