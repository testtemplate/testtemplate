package io.github.testtemplate.core.runner.listener;

import static java.lang.Boolean.TRUE;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.builder.Metadata;
import io.github.testtemplate.core.runner.RunnerTestInstantiator;

public class PreloadVariableListener implements RunnerTestInstantiator.Listener {

  @Override
  public void before(Test test) {
    test.getVariableNames().forEach(name -> {
      var variable = test.getVariable(name);
      if (TRUE.equals(variable.getMetadata(Metadata.Variable.PRELOAD, false))) {
        variable.getValue();
      }
    });
  }

  @Override
  public void after(Test test) {}

  @Override
  public void result(Test test, @Nullable Object result) {}

  @Override
  public void exception(Test test, Throwable exception) {}

  @Override
  public void variable(Test test, Variable variable) {}
}
