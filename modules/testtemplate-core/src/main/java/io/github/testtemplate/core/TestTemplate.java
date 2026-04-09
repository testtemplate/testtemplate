package io.github.testtemplate.core;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.function.ExceptionalFunction;

public final class TestTemplate<R> {

  private final ExceptionalFunction<ContextGiven, R> function;

  @Nullable
  private final StackTraceElement source;

  public TestTemplate(ExceptionalFunction<ContextGiven, R> function, @Nullable StackTraceElement source) {
    this.function = function;
    this.source = source;
  }

  public ExceptionalFunction<ContextGiven, R> getFunction() {
    return function;
  }

  @Nullable
  public StackTraceElement getSource() {
    return source;
  }
}
