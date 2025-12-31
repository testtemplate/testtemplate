package io.github.testtemplate;

import java.util.stream.Stream;

public interface TestSuiteFactory<S> {

  S getSuite(Stream<? extends Test> tests);

  interface Test {

    String getName();

  }

  interface TestItem extends Test {

    void execute();

  }

  interface TestGroup extends Test {

    Stream<? extends Test> getTests();

  }
}
