package io.github.testtemplate.junit;

import io.github.testtemplate.TestSuiteFactory;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import java.util.List;
import java.util.stream.Stream;

public class JUnitTestSuiteFactory implements TestSuiteFactory<Stream<DynamicNode>> {

  @Override
  public Stream<DynamicNode> getSuite(List<? extends Test> tests) {
    return tests.stream().map(t -> DynamicTest.dynamicTest(t.getName(), t::execute));
  }
}
