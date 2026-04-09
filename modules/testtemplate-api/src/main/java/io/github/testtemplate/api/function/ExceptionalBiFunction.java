package io.github.testtemplate.api.function;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ExceptionalBiFunction<T, U, R> {

  @Nullable R apply(T t, U u) throws Exception;

}
