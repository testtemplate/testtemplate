package io.github.testtemplate;

import java.util.function.Function;
import java.util.function.Supplier;

public interface AlternativeTestTemplateExceptBuilder<S, R> {

  AlternativeTestTemplateExceptBuilder<S, R> metadata(String key, Object value);

  AlternativeTestTemplateExceptPostBuilder<S, R> is(Function<ContextView, ?> value);

  default AlternativeTestTemplateExceptPostBuilder<S, R> is(Supplier<?> value) {
    return is(c -> value);
  }

  default AlternativeTestTemplateExceptPostBuilder<S, R> is(Object value) {
    return is(c -> value);
  }

  default AlternativeTestTemplateExceptPostBuilder<S, R> isNull() {
    return is(c -> null);
  }

  <M extends Extension<S, R>> M as(ExtensionFactory<S, R, M> factory);

  interface ExtensionFactory<S, R, M extends Extension<S, R>> {

    M getExtension(AlternativeTestTemplateExceptBuilder<S, R> builder, String variable);

  }

  interface Extension<S, R> {}

}
