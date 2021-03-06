package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.Context;
import io.github.testtemplate.DefaultTestTemplateBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface MockGivenInvokeBuilder<S, M> extends DefaultTestTemplateBuilder<S> {

  <T> MockGivenResponseBuilder<S, M, T> invoking(BiFunction<M, Context, T> method);

  default <T> MockGivenResponseBuilder<S, M, T> invoking(Function<M, T> method) {
    return invoking((m, c) -> method.apply(m));
  }
}
