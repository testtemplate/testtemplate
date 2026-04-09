package io.github.testtemplate.core.builder;

import io.github.testtemplate.api.suite.TestSuiteFactory;
import io.github.testtemplate.core.TestDefinition;

public interface TestInstantiator {

  <R> TestSuiteFactory.Test instantiate(TestDefinition<R> test);


}
