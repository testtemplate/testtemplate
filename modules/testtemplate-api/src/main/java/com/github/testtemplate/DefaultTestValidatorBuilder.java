package com.github.testtemplate;

public interface DefaultTestValidatorBuilder<S, R> {

  TestTemplateBuilder<S, R> then(ContextualValidator<R> validator);

}
