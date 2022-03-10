package com.github.testtemplate;

import java.util.function.Function;
import java.util.function.Supplier;

public interface DefaultTestTemplateGivenBuilder<S> {

  DefaultTestTemplateGivenBuilder<S> metadata(String key, Object value);

  DefaultTestTemplateGivenBuilder<S> preload();

  DefaultTestTemplateBuilder<S> is(Function<Context, ?> value);

  default DefaultTestTemplateBuilder<S> is(Supplier<?> value) {
    return is(c -> value.get());
  }

  default DefaultTestTemplateBuilder<S> is(Object value) {
    return is(c -> value);
  }

  default DefaultTestTemplateBuilder<S> isNull() {
    return is(c -> null);
  }

  <M extends Extension<S>> M as(ExtensionFactory<S, M> factory);

  interface ExtensionFactory<S, M extends Extension<S>> {

    M getExtension(DefaultTestTemplateGivenBuilder<S> builder, String variable);

  }

  interface Extension<S> {}

}
