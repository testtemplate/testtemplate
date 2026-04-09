package io.github.testtemplate.core.runner.listener;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.core.logger.TestLogger;
import io.github.testtemplate.core.runner.RunnerTestInstantiator;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class LoggerListener implements RunnerTestInstantiator.Listener {

  private static final String KEY_LOGGER = "io.github.testtemplate.core.runner.listener.LoggerListener.LOGGER";

  // Key must match RunnerTestInstantiator.RUNNER_METADATA_TEMPLATE_SOURCE
  private static final String KEY_TEMPLATE_SOURCE = "io.github.testtemplate.runner.templateSource";

  @Override
  public void before(Test test) {
    var logger = new TestLogger(test);
    var source = (StackTraceElement) test.getAttribute(KEY_TEMPLATE_SOURCE);
    if (source != null) {
      logger.setTemplateSource(source);
    }
    test.setAttribute(KEY_LOGGER, logger);
  }

  @Override
  public void after(Test test) {
    var logger = (TestLogger) Objects.requireNonNull(test.getAttribute(KEY_LOGGER));
    logger.logReport();
  }

  @Override
  public void result(Test test, @Nullable Object result) {
    var logger = (TestLogger) Objects.requireNonNull(test.getAttribute(KEY_LOGGER));
    logger.setResult(result);
  }

  @Override
  public void exception(Test test, Throwable exception) {
    var logger = (TestLogger) Objects.requireNonNull(test.getAttribute(KEY_LOGGER));
    logger.setException(exception);
  }

  @Override
  public void variable(Test test, Variable variable) {
    var logger = (TestLogger) Objects.requireNonNull(test.getAttribute(KEY_LOGGER));
    logger.setLoadedVariable(variable);
  }
}
