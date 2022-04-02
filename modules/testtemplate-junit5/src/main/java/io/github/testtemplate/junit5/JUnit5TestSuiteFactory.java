package io.github.testtemplate.junit5;

import io.github.testtemplate.TestSuiteFactory;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

public class JUnit5TestSuiteFactory implements TestSuiteFactory<List<DynamicNode>> {

  @Override
  public List<DynamicNode> getSuite(List<? extends Test> tests) {
    return tests.stream().map(t -> DynamicTest.dynamicTest(t.getName(), t::execute)).collect(toUnmodifiableList());
  }
}
