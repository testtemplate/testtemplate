package io.github.testtemplate;

import java.util.function.Function;
import java.util.function.Supplier;

public interface AlternativeTestTemplateExceptPostBuilder<S, R> extends AlternativeTestValidatorBuilder<S, R> {

  AlternativeTestTemplateExceptPostBuilder<S, R> or(Function<ContextView, ?> value);

  default AlternativeTestTemplateExceptPostBuilder<S, R> or(Supplier<?> value) {
    return or(c -> value);
  }

  default AlternativeTestTemplateExceptPostBuilder<S, R> or(Object value) {
    return or(c -> value);
  }

  default AlternativeTestTemplateExceptPostBuilder<S, R> orNull() {
    return or(c -> null);
  }
}
