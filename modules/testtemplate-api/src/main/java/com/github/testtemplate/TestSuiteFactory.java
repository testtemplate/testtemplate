package com.github.testtemplate;

import java.util.List;

public interface TestSuiteFactory<S> {

  S getSuite(List<? extends Test> tests);

  interface Test {

    String getName();

    void execute() throws Throwable;

  }
}
