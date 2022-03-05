package com.github.testtemplate;

public interface AlternativeTestValidationBuilder<S, R> {

  AlternativeTestTemplateExceptBuilder<S, R> except(String variable);

  TestTemplateBuilder<S, R> then(ContextualValidator<R> validator);

}
