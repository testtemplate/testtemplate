package io.github.testtemplate.extension.mockito;

import org.mockito.invocation.InvocationOnMock;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.builder.SetupBuilder;
import io.github.testtemplate.api.function.ExceptionalBiFunction;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

public interface MockitoSetupBuilder extends SetupBuilder.Extension {

  <M> InvokingStep<M> mock(Class<? extends M> classToMock);

  <M> InvokingStep<M> use(M mock);

  interface InvokingStep<M> extends SetupBuilder.GivenStep {

    <T> ResponseStep<M, T> invoking(ExceptionalBiFunction<M, ContextGiven, T> method);

    default <T> ResponseStep<M, T> invoking(ExceptionalFunction<M, T> method) {
      return invoking((mock, ctx) -> method.apply(mock));
    }
  }

  interface ResponseStep<M, T> {

    PostStep<M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response);

    default PostStep<M, T> willAnswer(ExceptionalFunction<InvocationOnMock, T> response) {
      return willAnswer((i, ctx) -> response.apply(i));
    }

    PostStep<M, T> willReturn(ExceptionalFunction<ContextGiven, T> response);

    default PostStep<M, T> willReturn(ExceptionalSupplier<T> response) {
      return willReturn(ctx -> response.get());
    }

    default PostStep<M, T> willReturn(T response) {
      return willReturn(ctx -> response);
    }

    PostStep<M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response);

    default PostStep<M, T> willThrow(ExceptionalSupplier<Throwable> response) {
      return willThrow(ctx -> response.get());
    }

    default PostStep<M, T> willThrow(Throwable response) {
      return willThrow(ctx -> response);
    }
  }

  interface PostStep<M, T> extends ResponseStep<M, T>, InvokingStep<M>, SetupBuilder.GivenStep {}
}
