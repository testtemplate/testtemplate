package io.github.testtemplate.core;

import io.github.testtemplate.TestSuiteFactory.Test;
import io.github.testtemplate.TestSuiteFactory.TestExecutor;
import io.github.testtemplate.TestSuiteFactory.TestGroup;
import io.github.testtemplate.core.runner.TestRunnerFactory;

import java.util.List;

public abstract class TestInstance<R> implements Test {

  public static <R> List<TestInstance<R>> allOf(List<TestDefinition<R>> tests, TestRunnerFactory runnerFactory) {
    return tests.stream().map(t -> of(t, runnerFactory)).toList();
  }

  public static <R> TestInstance<R> of(TestDefinition<R> test, TestRunnerFactory runnerFactory) {
    if (test.isParameterized()) {
      return new TestGroupInstance<>(test.getName(), allOf(test.deparameterize(), runnerFactory));
    } else {
      return new TestExecutorInstance<>(runnerFactory.getRunner(test));
    }
  }

  public static final class TestExecutorInstance<R> extends TestInstance<R> implements TestExecutor {

    private final TestRunnerFactory.TestRunner<R> runner;

    private TestExecutorInstance(TestRunnerFactory.TestRunner<R> runner) {
      this.runner = runner;
    }

    @Override
    public String getName() {
      return runner.getName();
    }

    @Override
    public void execute() {
      runner.run();
    }
  }

  public static final class TestGroupInstance<R> extends TestInstance<R> implements TestGroup {

    private final String name;

    private final List<TestInstance<R>> tests;

    private TestGroupInstance(String name, List<TestInstance<R>> tests) {
      this.name = name;
      this.tests = tests;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public List<? extends Test> getTests() {
      return tests;
    }
  }
}
