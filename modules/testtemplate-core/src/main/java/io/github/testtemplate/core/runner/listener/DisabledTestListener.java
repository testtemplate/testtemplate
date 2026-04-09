package io.github.testtemplate.core.runner.listener;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.builder.Metadata;
import io.github.testtemplate.core.runner.RunnerTestInstantiator;

import org.jspecify.annotations.Nullable;
import org.opentest4j.TestAbortedException;

import static java.lang.Boolean.TRUE;

public class DisabledTestListener implements RunnerTestInstantiator.Listener {

  @Override
  public void before(Test test) {
    if (TRUE.equals(test.getAttribute(Metadata.Test.DISABLED, false))) {
      String reason = (String) test.getAttribute(Metadata.Test.DISABLED_REASON, "no reason");
      throw new TestAbortedException("The test is disabled: " + reason);
    }
  }

  @Override
  public void after(Test test) {}

  @Override
  public void result(Test test, @Nullable Object result) {}

  @Override
  public void exception(Test test, Throwable exception) {}

  @Override
  public void variable(Test test, Variable variable) {

  }
}
