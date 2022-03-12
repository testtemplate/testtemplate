package com.github.testtemplate.extension.mockito;

import com.github.testtemplate.Context;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.function.Function;
import java.util.function.Supplier;

public interface MockGivenResponseBuilder<S, M, T> {

  MockGivenPostResponseBuilder<S, M, T> willAnswer(ContextualAnswer<T> response);

  default MockGivenPostResponseBuilder<S, M, T> willAnswer(Answer<T> response) {
    return willAnswer((i, c) -> response.answer(i));
  }

  MockGivenPostResponseBuilder<S, M, T> willReturn(Function<Context, T> response);

  default MockGivenPostResponseBuilder<S, M, T> willReturn(Supplier<T> response) {
    return willReturn(c -> response.get());
  }

  default MockGivenPostResponseBuilder<S, M, T> willReturn(T response) {
    return willReturn(c -> response);
  }

  MockGivenPostResponseBuilder<S, M, T> willThrow(Function<Context, Throwable> response);

  default MockGivenPostResponseBuilder<S, M, T> willThrow(Supplier<Throwable> response) {
    return willThrow(c -> response.get());
  }

  default MockGivenPostResponseBuilder<S, M, T> willThrow(Throwable response) {
    return willThrow(c -> response);
  }

  interface ContextualAnswer<T> {

    T answer(InvocationOnMock invocation, Context context) throws Throwable;

  }
}
