package io.github.testtemplate;

public interface TestTemplatePreBuilder<S> {

  TestTemplatePreBuilder<S> disabledAll(String reason);

  default TestTemplatePreBuilder<S> disabledAll() {
    return disabledAll("unknown reason");
  }

  DefaultTestTemplatePreBuilder<S> defaultTest(String name);

}
