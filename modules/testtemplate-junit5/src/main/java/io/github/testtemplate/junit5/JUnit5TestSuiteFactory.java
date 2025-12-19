package io.github.testtemplate.junit5;

import io.github.testtemplate.TestSuiteFactory;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

public class JUnit5TestSuiteFactory implements TestSuiteFactory<List<DynamicNode>> {

  @Override
  public List<DynamicNode> getSuite(List<? extends Test> tests) {
    return mapTests(tests).collect(toUnmodifiableList());
  }

  private static Stream<? extends DynamicNode> mapTests(List<? extends Test> tests) {
    return tests.stream().map(JUnit5TestSuiteFactory::mapTest);
  }

  private static DynamicNode mapTest(Test test) {
    return switch (test) {
      case TestExecutor executor -> DynamicTest.dynamicTest(executor.getName(), executor::execute);
      case TestGroup group -> DynamicContainer.dynamicContainer(group.getName(), mapTests(group.getTests()));
      default -> throw new IllegalArgumentException("Unknown Test " + test);
    };
  }
}
