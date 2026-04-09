package io.github.testtemplate.extension.mockito;

import org.mockito.invocation.InvocationOnMock;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.builder.AlternativeBuilder;
import io.github.testtemplate.api.function.ExceptionalBiFunction;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

public interface MockitoAlternativeBuilder<S, R> extends AlternativeBuilder.Extension<S, R> {

  <M, T> ResponseStep<S, R, M, T> invoking(ExceptionalBiFunction<M, Context, T> method);

  default <M, T> ResponseStep<S, R, M, T> invoking(ExceptionalFunction<M, T> method) {
    return invoking((mock, ctx) -> method.apply(mock));
  }

  interface ResponseStep<S, R, M, T> {

    PostStep<S, R, M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, Context, T> response);

    default PostStep<S, R, M, T> willAnswer(ExceptionalFunction<InvocationOnMock, T> response) {
      return willAnswer((i, ctx) -> response.apply(i));
    }

    PostStep<S, R, M, T> willReturn(ExceptionalFunction<Context, T> response);

    default PostStep<S, R, M, T> willReturn(ExceptionalSupplier<T> response) {
      return willReturn(ctx -> response.get());
    }

    default PostStep<S, R, M, T> willReturn(T response) {
      return willReturn(ctx -> response);
    }

    PostStep<S, R, M, T> willThrow(ExceptionalFunction<Context, Throwable> response);

    default PostStep<S, R, M, T> willThrow(ExceptionalSupplier<Throwable> response) {
      return willThrow(ctx -> response.get());
    }

    default PostStep<S, R, M, T> willThrow(Throwable response) {
      return willThrow(ctx -> response);
    }
  }

  interface PostStep<S, R, M, T>
      extends ResponseStep<S, R, M, T>, AlternativeBuilder.ExceptStep<S, R> {}
}
