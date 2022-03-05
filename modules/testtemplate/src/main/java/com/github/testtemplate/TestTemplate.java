package com.github.testtemplate;

import com.github.testtemplate.core.builder.TestBuilder;
import com.github.testtemplate.junit5.JUnit5TestSuiteFactory;

import org.junit.jupiter.api.DynamicNode;

import java.util.List;

public final class TestTemplate {

  private TestTemplate() {}

  public static TestTemplatePreBuilder<List<DynamicNode>> junit5() {
    return TestBuilder.builder(new JUnit5TestSuiteFactory());
  }

  public static DefaultTestTemplateBuilder<List<DynamicNode>> defaultTest(String name) {
    return junit5().defaultTest(name);
  }
}
