package io.github.testtemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface AlternativeTestTemplateExceptBuilder<S, R> {

  AlternativeTestTemplateExceptBuilder<S, R> metadata(String key, Object value);

  AlternativeTestValidatorBuilder<S, R> is(Function<ContextView, ?> value);

  default AlternativeTestValidatorBuilder<S, R> is(Supplier<?> value) {
    return is(c -> value.get());
  }

  default AlternativeTestValidatorBuilder<S, R> is(Object value) {
    return is(c -> value);
  }

  default AlternativeTestValidatorBuilder<S, R> isNull() {
    return is(c -> null);
  }

  AlternativeTestValidatorBuilder<S, R> isAnyOf(List<Function<ContextView, ?>> values);

  default AlternativeTestValidatorBuilder<S, R> isAnyOf(Supplier<?>... values) {
    var functions = new ArrayList<Function<ContextView, ?>>();
    for (Supplier<?> value : values) {
      functions.add(c -> value.get());
    }
    return isAnyOf(functions);
  }

  default AlternativeTestValidatorBuilder<S, R> isAnyOf(Object... values) {
    var functions = new ArrayList<Function<ContextView, ?>>();
    for (Object value : values) {
      functions.add(c -> value);
    }

    return isAnyOf(functions);
  }

  <M extends Extension<S, R>> M as(ExtensionFactory<S, R, M> factory);

  interface ExtensionFactory<S, R, M extends Extension<S, R>> {

    M getExtension(AlternativeTestTemplateExceptBuilder<S, R> builder, String variable);

  }

  interface Extension<S, R> {}

}
