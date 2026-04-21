package io.github.testtemplate.core;

import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.function.ExceptionalConsumer;

import org.jspecify.annotations.Nullable;

public final class TestValidator<R> {

  private final ExceptionalConsumer<ContextResult<R>> function;

  @Nullable
  private final StackTraceElement source;

  public TestValidator(ExceptionalConsumer<ContextResult<R>> function, @Nullable StackTraceElement source) {
    this.function = function;
    this.source = source;
  }

  public ExceptionalConsumer<ContextResult<R>> getFunction() {
    return function;
  }

  @Nullable
  public StackTraceElement getSource() {
    return source;
  }
}
