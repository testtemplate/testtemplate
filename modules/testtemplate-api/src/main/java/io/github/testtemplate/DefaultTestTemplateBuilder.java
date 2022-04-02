package io.github.testtemplate;

public interface DefaultTestTemplateBuilder<S> {

  DefaultTestTemplateGivenBuilder<S> given(String variable);

  <R> DefaultTestValidatorBuilder<S, R> when(ContextualTemplate<R> template);

}
