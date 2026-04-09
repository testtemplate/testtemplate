package io.github.testtemplate.api.function;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ExceptionalSupplier<T> {

  @Nullable T get() throws Exception;

}
