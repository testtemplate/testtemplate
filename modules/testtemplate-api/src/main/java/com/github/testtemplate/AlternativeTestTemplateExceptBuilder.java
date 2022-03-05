package com.github.testtemplate;

import java.util.function.Function;
import java.util.function.Supplier;

public interface AlternativeTestTemplateExceptBuilder<S, R> {

  AlternativeTestTemplateExceptBuilder<S, R> metadata(String key, Object value);

  AlternativeTestValidationBuilder<S, R> is(Function<ContextView, ?> value);

  default AlternativeTestValidationBuilder<S, R> is(Supplier<?> value) {
    return is(c -> value);
  }

  default AlternativeTestValidationBuilder<S, R> is(Object value) {
    return is(c -> value);
  }

  default AlternativeTestValidationBuilder<S, R> isNull() {
    return is(c -> null);
  }
}
