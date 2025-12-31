package io.github.testtemplate.junit;

import io.github.testtemplate.TestSuiteFactory;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import java.util.stream.Stream;

public class JUnitTestSuiteFactory implements TestSuiteFactory<Stream<DynamicNode>> {

  @Override
  public Stream<DynamicNode> getSuite(Stream<? extends Test> tests) {
    return tests.map(this::toNode);
  }

  private DynamicNode toNode(Test test) {
    return switch (test) {
      case TestItem item -> DynamicTest.dynamicTest(item.getName(), item::execute);
      case TestGroup group -> DynamicContainer.dynamicContainer(group.getName(), getSuite(group.getTests()));
      default -> throw new IllegalArgumentException("Unknown test: " + test.getClass().getName());
    };
  }
}
