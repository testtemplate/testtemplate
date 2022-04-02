package io.github.testtemplate.core.runner;

import io.github.testtemplate.TestListener;
import io.github.testtemplate.core.TestDefinition;

import java.util.ArrayList;
import java.util.List;

public final class TestRunner {

  private final List<TestListener> listeners = new ArrayList<>();

  public TestRunner(List<TestListener> listeners) {
    this.listeners.addAll(listeners);
  }

  public <R> void run(TestDefinition<R> test) {
    var variableResolver = new RunnerVariableResolver(test.getVariables(), test.getModifiers());

    var testContext = new RunnerTest(
        test.getName(),
        test.getType(),
        variableResolver,
        test.getAttributes());

    variableResolver.registerListener((name, type, value) ->
        listeners.forEach(listener -> listener.variable(testContext, name, type, value)));

    listeners.forEach(listener -> listener.before(testContext));
    try {
      RunnerValidatorContextView<R> validatorContext;
      try {
        RunnerContext context = new RunnerContext(variableResolver);
        R result = test.getTemplate().run(context);
        listeners.forEach(listener -> listener.result(testContext, result));
        validatorContext = new RunnerResultValidatorContextView<>(variableResolver, result);
      } catch (TestRunnerException exception) {
        throw exception;
      } catch (Throwable exception) {
        listeners.forEach(listener -> listener.exception(testContext, exception));
        validatorContext = new RunnerExceptionValidatorContextView<>(variableResolver, exception);
      }

      test.getValidator().validate(validatorContext);
    } finally {
      listeners.forEach(listener -> listener.after(testContext));
    }
  }
}
