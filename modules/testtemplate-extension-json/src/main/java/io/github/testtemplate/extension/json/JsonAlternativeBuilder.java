package io.github.testtemplate.extension.json;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.builder.AlternativeBuilder;

public interface JsonAlternativeBuilder<S, R> extends AlternativeBuilder.Extension<S, R> {

  ValueStep<S, R> path(String path);

  interface ValueStep<S, R> {

    AlternativeBuilder.ExceptStep<S, R> is(@Nullable Object value);

    AlternativeBuilder.ExceptStep<S, R> isAbsent();

    AlternativeBuilder.ExceptStep<S, R> hasExtra(@Nullable Object value);

    AlternativeBuilder.ExceptStep<S, R> hasExtra(String key, @Nullable Object value);

  }
}
