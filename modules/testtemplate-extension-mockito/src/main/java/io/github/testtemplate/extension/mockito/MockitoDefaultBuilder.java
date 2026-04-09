package io.github.testtemplate.extension.mockito;

import org.mockito.invocation.InvocationOnMock;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.builder.DefaultBuilder;
import io.github.testtemplate.api.function.ExceptionalBiFunction;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

public interface MockitoDefaultBuilder<S> extends DefaultBuilder.Extension<S> {

  <M> InvokingStep<S, M> mock(Class<? extends M> classToMock);

  <M> InvokingStep<S, M> use(M mock);

  interface InvokingStep<S, M> extends DefaultBuilder.GivenStep<S> {

    <T> ResponseStep<S, M, T> invoking(ExceptionalBiFunction<M, ContextGiven, T> method);

    default <T> ResponseStep<S, M, T> invoking(ExceptionalFunction<M, T> method) {
      return invoking((mock, ctx) -> method.apply(mock));
    }
  }

  interface ResponseStep<S, M, T> {

    PostStep<S, M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response);

    default PostStep<S, M, T> willAnswer(ExceptionalFunction<InvocationOnMock, T> response) {
      return willAnswer((i, ctx) -> response.apply(i));
    }

    PostStep<S, M, T> willReturn(ExceptionalFunction<ContextGiven, T> response);

    default PostStep<S, M, T> willReturn(ExceptionalSupplier<T> response) {
      return willReturn(ctx -> response.get());
    }

    default PostStep<S, M, T> willReturn(T response) {
      return willReturn(ctx -> response);
    }

    PostStep<S, M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response);

    default PostStep<S, M, T> willThrow(ExceptionalSupplier<Throwable> response) {
      return willThrow(ctx -> response.get());
    }

    default PostStep<S, M, T> willThrow(Throwable response) {
      return willThrow(ctx -> response);
    }
  }

  interface PostStep<S, M, T> extends ResponseStep<S, M, T>, InvokingStep<S, M>, DefaultBuilder.GivenStep<S> {}

}
