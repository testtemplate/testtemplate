package com.github.testtemplate.extension.mockito;

import com.github.testtemplate.AlternativeTestTemplateExceptBuilder;
import com.github.testtemplate.ContextView;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface MockExceptInvokeBuilder<S, R> extends AlternativeTestTemplateExceptBuilder.Extension<S, R> {

  <M, T> MockExpectResponseBuilder<S, R, M, T> invoking(BiFunction<M, ContextView, T> method);

  default <M, T> MockExpectResponseBuilder<S, R, M, T> invoking(Function<M, T> method) {
    return invoking((m, c) -> method.apply(m));
  }
}
