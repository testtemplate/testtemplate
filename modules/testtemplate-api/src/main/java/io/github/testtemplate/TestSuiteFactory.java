package io.github.testtemplate;

import java.util.List;

public interface TestSuiteFactory<S> {

  S getSuite(List<? extends Test> tests);

  interface Test {

    String getName();

  }

  interface TestExecutor extends Test {

    void execute() throws Throwable;

  }

  interface TestGroup extends Test {

    List<Test> getTests();

  }
}
