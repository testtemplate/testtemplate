package io.github.testtemplate.core;

import io.github.testtemplate.TestSuiteFactory;
import io.github.testtemplate.core.runner.TestRunner;

public final class TestItemInstance<R> implements TestSuiteFactory.TestItem {

  private final TestDefinition<R> test;

  private final TestRunner runner;

  public TestItemInstance(TestDefinition<R> test, TestRunner runner) {
    this.test = test;
    this.runner = runner;
  }

  @Override
  public String getName() {
    return test.getName();
  }

  @Override
  public void execute() {
    runner.run(test);
  }
}
