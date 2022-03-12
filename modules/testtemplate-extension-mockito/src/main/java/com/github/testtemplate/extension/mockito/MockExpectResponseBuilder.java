package com.github.testtemplate.extension.mockito;

import com.github.testtemplate.ContextView;

import org.mockito.invocation.InvocationOnMock;

import java.util.function.Function;

public interface MockExpectResponseBuilder<S, R, M, T> {

  MockExpectPostResponseBuilder<S, R, M, T> willAnswer(ContextualAnswer<T> response);

  MockExpectPostResponseBuilder<S, R, M, T> willReturn(Function<ContextView, T> response);

  MockExpectPostResponseBuilder<S, R, M, T> willThrow(Function<ContextView, Throwable> response);

  interface ContextualAnswer<T> {

    T answer(InvocationOnMock invocation, ContextView context) throws Throwable;

  }
}
