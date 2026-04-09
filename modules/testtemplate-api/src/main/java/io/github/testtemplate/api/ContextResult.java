package io.github.testtemplate.api;

import org.jspecify.annotations.Nullable;

public interface ContextResult<T> extends ContextExtension {

  @Nullable T result();

  Throwable exception();

}
