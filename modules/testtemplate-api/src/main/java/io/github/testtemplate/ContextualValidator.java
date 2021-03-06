package io.github.testtemplate;

@FunctionalInterface
public interface ContextualValidator<R> {

  void validate(ValidatorContextView<R> context);

}
