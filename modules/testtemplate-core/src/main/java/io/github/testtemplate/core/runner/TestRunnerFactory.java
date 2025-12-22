package io.github.testtemplate.core.runner;

import io.github.testtemplate.TestListener;
import io.github.testtemplate.core.TestDefinition;

import org.apache.commons.text.StringSubstitutor;

import java.util.ArrayList;
import java.util.List;

public final class TestRunnerFactory {

  private final List<TestListener> listeners = new ArrayList<>();

  public TestRunnerFactory(List<TestListener> listeners) {
    this.listeners.addAll(listeners);
  }

  public <R> TestRunner<R> getRunner(TestDefinition<R> testDefinition) {
    return new TestRunner<>(testDefinition);
  }

  public final class TestRunner<R> {

    private final TestDefinition<R> test;

    private final String name;

    private final RunnerVariableResolver variableResolver;
    private final RunnerTest testContext;

    public TestRunner(TestDefinition<R> test) {
      this.test = test;

      this.variableResolver = new RunnerVariableResolver(test.getVariables(), test.getModifiers());

      this.name = resolveName(test.getName(), variableResolver);

      this.testContext = new RunnerTest(
          this.name,
          test.getType(),
          variableResolver,
          test.getAttributes());

      variableResolver.registerListener((name, type, value) ->
          listeners.forEach(listener -> listener.variable(testContext, name, type, value)));
    }

    public String getName() {
      return name;
    }

    public void run() {
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

    private static String resolveName(String name, RunnerVariableResolver variableResolver) {
      var substitutor = new StringSubstitutor(s -> String.valueOf(variableResolver.getVariable(s).getValue()));
      return substitutor.replace(name);
    }
  }
}
