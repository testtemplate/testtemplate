package io.github.testtemplate.api.function;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ExceptionalFunction<T, R> {

  @Nullable R apply(T t) throws Exception;

}
