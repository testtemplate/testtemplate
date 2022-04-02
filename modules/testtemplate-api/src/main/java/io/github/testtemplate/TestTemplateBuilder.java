package io.github.testtemplate;

public interface TestTemplateBuilder<S, R> {

  AlternativeTestTemplatePreBuilder<S, R> test(String name);

  S suite();

}
