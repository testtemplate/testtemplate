package com.github.testtemplate;

public interface TestTemplateBuilder<S, R> {

  AlternativeTestTemplatePreBuilder<S, R> test(String name);

  S suite();

}
