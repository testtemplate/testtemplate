package io.github.testtemplate.core.runner.listener;

import java.util.ServiceLoader;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.listener.TestListener;
import io.github.testtemplate.core.runner.RunnerTestInstantiator;

public class CustomTestListener implements RunnerTestInstantiator.Listener {

  // TODO Check if it's better to have it as a singleton?
  private final ServiceLoader<TestListener> loader = ServiceLoader.load(TestListener.class);

  @Override
  public void before(Test test) {
    loader.forEach(listener -> listener.before(test));
  }

  @Override
  public void after(Test test) {
    loader.forEach(listener -> listener.after(test));
  }

  @Override
  public void result(Test test, @Nullable Object result) {
    loader.forEach(listener -> listener.result(test, result));
  }

  @Override
  public void exception(Test test, Throwable exception) {
    loader.forEach(listener -> listener.exception(test, exception));
  }

  @Override
  public void variable(Test test, Variable variable) {
    loader.forEach(listener -> listener.variable(test, variable));
  }
}
