package io.github.testtemplate;

public interface AlternativeTestValidatorBuilder<S, R> {

  AlternativeTestTemplateExceptBuilder<S, R> except(String variable);

  TestTemplateBuilder<S, R> then(ContextualValidator<R> validator);

}
