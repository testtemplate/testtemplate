package io.github.testtemplate.core.runner;

import io.github.testtemplate.TestListener;
import io.github.testtemplate.TestSuiteFactory;
import io.github.testtemplate.core.TestDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class TestRunner {

  private final List<TestListener> listeners = new ArrayList<>();

  public TestRunner(List<TestListener> listeners) {
    this.listeners.addAll(listeners);
  }

  public <R> TestSuiteFactory.Test toInstance(TestDefinition<R> test) {
    if (test.isParameterized()) {
      return new TestGroupInstance<>(test);
    } else {
      return new TestItemInstance<>(test);
    }
  }

  private final class TestItemInstance<R> implements TestSuiteFactory.TestItem {

    private final TestDefinition<R> test;
    private final String resolvedName;
    private final RunnerVariableResolver variableResolver;
    private final RunnerTest testContext;

    private TestItemInstance(TestDefinition<R> test) {
      this.test = test;
      this.variableResolver = new RunnerVariableResolver(test.getVariables(), test.getModifiers());
      this.resolvedName = resolveName(test.getName(), variableResolver);
      this.testContext = new RunnerTest(resolvedName, test.getType(), variableResolver, test.getAttributes());
      variableResolver.registerListener((name, type, value, metadata) ->
          listeners.forEach(listener -> listener.variable(testContext, name, type, value, metadata)));
    }

    @Override
    public String getName() {
      return resolvedName;
    }

    @Override
    public void execute() {
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
      return TestNameSubstitutor.resolveName(name, variableResolver);
    }
  }

  private final class TestGroupInstance<R> implements TestSuiteFactory.TestGroup {

    private final TestDefinition<R> test;

    private final String resolvedName;

    private TestGroupInstance(TestDefinition<R> test) {
      this.test = test;
      var variableResolver = new RunnerVariableResolver(test.getVariables(), test.getModifiers());
      this.resolvedName = resolveName(test.getName(), variableResolver);
    }

    @Override
    public String getName() {
      return resolvedName;
    }

    @Override
    public Stream<? extends TestSuiteFactory.Test> getTests() {
      return test.deparameterize().map(t -> toInstance(t));
    }

    private static String resolveName(String name, RunnerVariableResolver variableResolver) {
      return TestNameSubstitutor.resolveName(name, variableResolver);
    }
  }
}
