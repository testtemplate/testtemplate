package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.builder.AlternativeBuilder;
import io.github.testtemplate.api.builder.DefaultBuilder;
import io.github.testtemplate.api.builder.SetupBuilder;
import io.github.testtemplate.api.function.ExceptionalBiFunction;
import io.github.testtemplate.api.function.ExceptionalConsumer;
import io.github.testtemplate.api.function.ExceptionalFunction;

import org.jspecify.annotations.Nullable;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class MockitoExtension<S, R> implements
    DefaultBuilder.ExtensionFactory<S, MockitoDefaultBuilder<S>>,
    AlternativeBuilder.ExtensionFactory<S, R, MockitoAlternativeBuilder<S, R>>,
    SetupBuilder.ExtensionFactory<MockitoSetupBuilder> {

  @Nullable
  private static MockitoExtension<?, ?> instance;

  @Override
  public MockitoDefaultBuilder<S> getExtension(
      DefaultBuilder.GivenStep.MetadataStep<S> builder,
      String variable) {
    return new InnerMockitoDefaultBuilder<>(builder, variable);
  }

  @Override
  public MockitoAlternativeBuilder<S, R> getExtension(
      AlternativeBuilder.ExceptStep.MetadataStep<S, R> builder,
      String variable) {
    return new InnerMockitoAlternativeBuilder<>(builder, variable);
  }

  @Override
  public MockitoSetupBuilder getExtension(
      SetupBuilder.MetadataStep builder,
      String variable) {
    return new InnerMockitoSetupBuilder(builder, variable);
  }

  @SuppressWarnings("unchecked")
  public static <S, R> MockitoExtension<S, R> mock() {
    if (instance == null) {
      instance = new MockitoExtension<>();
    }

    return (MockitoExtension<S, R>) instance;
  }

  private static final class InnerMockitoDefaultBuilder<S> implements MockitoDefaultBuilder<S> {

    private final DefaultBuilder.GivenStep.MetadataStep<S> builder;

    private final String variable;

    private InnerMockitoDefaultBuilder(DefaultBuilder.GivenStep.MetadataStep<S> builder, String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    @Override
    public <M> InvokingStep<S, M> mock(Class<? extends M> classToMock) {
      return new InnerInvokingStep<>(ctx -> Mockito.mock(classToMock, variable));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> InvokingStep<S, M> use(M mock) {
      if (!Mockito.mockingDetails(mock).isMock()) {
        throw new IllegalArgumentException("The object must be a mock (or a spy)"); // TODO Replace Exception
      }

      return new InnerInvokingStep<>(ctx -> {
        Mockito.reset(mock);
        return mock;
      });
    }

    private final class InnerInvokingStep<M> implements InvokingStep<S, M> {

      private final ExceptionalFunction<ContextGiven, M> supplier;

      private InnerInvokingStep(ExceptionalFunction<ContextGiven, M> supplier) {
        this.supplier = supplier;
      }

      @Override
      public <T> ResponseStep<S, M, T> invoking(ExceptionalBiFunction<M, ContextGiven, T> method) {
        return new InnerResponseStep<>(supplier, method);
      }

      @Override
      public ExtensionStep<S> given(String variable) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
//            .preload()
//            .metadata("io.github.testtemplate.extension.mockito.mock", true)
            .is(supplier)
            .given(variable);
      }

      @Override
      public <R> DefaultBuilder.ThenStep<S, R> when(ExceptionalFunction<ContextGiven, R> template) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
//            .preload()
//            .metadata("io.github.testtemplate.extension.mockito.mock", true)
            .is(supplier)
            .when(template);
      }
    }

    private final class InnerResponseStep<M, T> implements ResponseStep<S, M, T> {

      private final ExceptionalFunction<ContextGiven, M> supplier;

      private final ExceptionalBiFunction<M, ContextGiven, T> method;

      private InnerResponseStep(
          ExceptionalFunction<ContextGiven, M> supplier,
          ExceptionalBiFunction<M, ContextGiven, T> method) {
        this.supplier = supplier;
        this.method = method;
      }

      @Override
      public PostStep<S, M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response) {
        return new InnerPostStep<>(supplier, method, ctx -> i -> requireNonNull(response.apply(i, ctx)));
      }

      @Override
      public PostStep<S, M, T> willReturn(ExceptionalFunction<ContextGiven, T> response) {
        return new InnerPostStep<>(supplier, method, ctx -> new Returns(response.apply(ctx)));
      }

      @Override
      public PostStep<S, M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response) {
        return new InnerPostStep<>(supplier, method, ctx -> new ThrowsException(response.apply(ctx)));
      }
    }

    private final class InnerPostStep<M, T> implements PostStep<S, M, T> {

      private final ExceptionalFunction<ContextGiven, M> supplier;

      private final ExceptionalBiFunction<M, ContextGiven, T> method;

      private final List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers = new ArrayList<>();

      private InnerPostStep(
          ExceptionalFunction<ContextGiven, M> supplier,
          ExceptionalBiFunction<M, ContextGiven, T> method,
          ExceptionalFunction<ContextGiven, Answer<Object>> answer) {
        this.supplier = supplier;
        this.method = method;
        this.answers.add(answer);
      }

      @Override
      public PostStep<S, M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response) {
        this.answers.add(ctx -> i -> requireNonNull(response.apply(i, ctx)));
        return this;
      }

      @Override
      public PostStep<S, M, T> willReturn(ExceptionalFunction<ContextGiven, T> response) {
        this.answers.add(ctx -> new Returns(response.apply(ctx)));
        return this;
      }

      @Override
      public PostStep<S, M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response) {
        this.answers.add(ctx -> new ThrowsException(response.apply(ctx)));
        return this;
      }

      @Override
      public <T2> ResponseStep<S, M, T2> invoking(ExceptionalBiFunction<M, ContextGiven, T2> method) {
        return new InnerResponseStep<>(buildSupplier(), method);
      }

      @Override
      public ExtensionStep<S> given(String variable) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .given(variable);
      }

      @Override
      public <R> DefaultBuilder.ThenStep<S, R> when(ExceptionalFunction<ContextGiven, R> template) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .when(template);
      }

      private ExceptionalFunction<ContextGiven, M> buildSupplier() {
        return ctx -> {
          try {
            var mock = requireNonNull(supplier.apply(ctx));
            var answerIterator = answers.iterator();
            var stubber = Mockito.lenient().doAnswer(answerIterator.next().apply(ctx));
            while (answerIterator.hasNext()) {
              answerIterator.next().apply(ctx);
            }
            method.apply(stubber.when(mock), ctx);
            return mock;
          } catch (Exception e) {
            throw new MockitoExtensionException("Caught exception", e); // TODO Add better message
          }
        };
      }
    }
  }

  private static final class InnerMockitoAlternativeBuilder<S, R> implements MockitoAlternativeBuilder<S, R> {

    private final AlternativeBuilder.ExceptStep.MetadataStep<S, R> builder;

    private final String variable;

    private InnerMockitoAlternativeBuilder(AlternativeBuilder.ExceptStep.MetadataStep<S, R> builder, String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    @Override
    public <M, T> ResponseStep<S, R, M, T> invoking(ExceptionalBiFunction<M, Context, T> method) {
      return new InnerResponseStep<>(method);
    }

    private final class InnerResponseStep<M, T> implements ResponseStep<S, R, M, T> {

      private final ExceptionalBiFunction<M, Context, T> method;

      private InnerResponseStep(ExceptionalBiFunction<M, Context, T> method) {
        this.method = method;
      }

      @Override
      public PostStep<S, R, M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, Context, T> response) {
        return new InnerPostStep<>(method, ctx -> i -> requireNonNull(response.apply(i, ctx)));
      }

      @Override
      public PostStep<S, R, M, T> willReturn(ExceptionalFunction<Context, T> response) {
        return new InnerPostStep<>(method, ctx -> new Returns(response.apply(ctx)));
      }

      @Override
      public PostStep<S, R, M, T> willThrow(ExceptionalFunction<Context, Throwable> response) {
        return new InnerPostStep<>(method, ctx -> new ThrowsException(response.apply(ctx)));
      }
    }

    private final class InnerPostStep<M, T> implements PostStep<S, R, M, T> {

      private final ExceptionalBiFunction<M, Context, T> method;

      private final List<ExceptionalFunction<Context, Answer<Object>>> answers = new ArrayList<>();

      private InnerPostStep(
          ExceptionalBiFunction<M, Context, T> method,
          ExceptionalFunction<Context, Answer<Object>> answer) {
        this.method = method;
        this.answers.add(answer);
      }

      @Override
      public MockitoAlternativeBuilder.PostStep<S, R, M, T> willAnswer(
          ExceptionalBiFunction<InvocationOnMock, Context, T> response) {
        this.answers.add(ctx -> i -> requireNonNull(response.apply(i, ctx)));
        return this;
      }

      @Override
      public MockitoAlternativeBuilder.PostStep<S, R, M, T> willReturn(
          ExceptionalFunction<Context, T> response) {
        this.answers.add(ctx -> new Returns(response.apply(ctx)));
        return this;
      }

      @Override
      public MockitoAlternativeBuilder.PostStep<S, R, M, T> willThrow(
          ExceptionalFunction<Context, Throwable> response) {
        this.answers.add(ctx -> new ThrowsException(response.apply(ctx)));
        return this;
      }

      @Override
      public ExtensionStep<S, R> except(
          String variable) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .except(variable);
      }

      @Override
      public Value2Step<S, R> except(
          String variable1,
          String variable2) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .except(variable1, variable2);
      }

      @Override
      public Value3Step<S, R> except(
          String variable1,
          String variable2,
          String variable3) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .except(variable1, variable2, variable3);
      }

      @Override
      public Value4Step<S, R> except(
          String variable1,
          String variable2,
          String variable3,
          String variable4) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .except(variable1, variable2, variable3, variable4);
      }

      @Override
      public Value5Step<S, R> except(
          String variable1,
          String variable2,
          String variable3,
          String variable4,
          String variable5) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .except(variable1, variable2, variable3, variable4, variable5);
      }

      @Override
      public ValueNStep<S, R> except(List<String> variables) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .except(variables);
      }

      @Override
      public AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .then(validator);
      }

      private ExceptionalFunction<Context, M> buildSupplier() {
        return ctx -> {
          try {
            M mock = requireNonNull(ctx.get(variable));
            var answerIterator = answers.iterator();
            var stubber = Mockito.lenient().doAnswer(answerIterator.next().apply(ctx));
            while (answerIterator.hasNext()) {
              answerIterator.next().apply(ctx);
            }
            method.apply(stubber.when(mock), ctx);
            return mock;
          } catch (Exception e) {
            throw new MockitoExtensionException("Caught exception", e); // TODO Add better message
          }
        };
      }
    }
  }

  private static final class InnerMockitoSetupBuilder implements MockitoSetupBuilder {

    private final SetupBuilder.MetadataStep builder;

    private final String variable;

    private InnerMockitoSetupBuilder(SetupBuilder.MetadataStep builder, String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    @Override
    public <M> InvokingStep<M> mock(Class<? extends M> classToMock) {
      return new InnerInvokingStep<>(ctx -> Mockito.mock(classToMock, variable));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> InvokingStep<M> use(M mock) {
      if (!Mockito.mockingDetails(mock).isMock()) {
        throw new IllegalArgumentException("The object must be a mock (or a spy)"); // TODO Replace Exception
      }

      // Be sure there is no stubbing outside the test builder because we will reset the mock after each test.
      Mockito.reset(mock);

      return new InnerInvokingStep<>(ctx -> mock);
    }

    private final class InnerInvokingStep<M> implements InvokingStep<M> {

      private final ExceptionalFunction<ContextGiven, M> supplier;

      private InnerInvokingStep(ExceptionalFunction<ContextGiven, M> supplier) {
        this.supplier = supplier;
      }

      @Override
      public <T> ResponseStep<M, T> invoking(ExceptionalBiFunction<M, ContextGiven, T> method) {
        return new InnerResponseStep<>(supplier, method);
      }

      @Override
      public SetupBuilder.ExtensionStep given(String variable) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(supplier)
            .given(variable);
      }
    }

    private final class InnerResponseStep<M, T> implements ResponseStep<M, T> {

      private final ExceptionalFunction<ContextGiven, M> supplier;

      private final ExceptionalBiFunction<M, ContextGiven, T> method;

      private InnerResponseStep(
          ExceptionalFunction<ContextGiven, M> supplier,
          ExceptionalBiFunction<M, ContextGiven, T> method) {
        this.supplier = supplier;
        this.method = method;
      }

      @Override
      public PostStep<M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response) {
        return new InnerPostStep<>(supplier, method, ctx -> i -> requireNonNull(response.apply(i, ctx)));
      }

      @Override
      public PostStep<M, T> willReturn(ExceptionalFunction<ContextGiven, T> response) {
        return new InnerPostStep<>(supplier, method, ctx -> new Returns(response.apply(ctx)));
      }

      @Override
      public PostStep<M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response) {
        return new InnerPostStep<>(supplier, method, ctx -> new ThrowsException(response.apply(ctx)));
      }
    }

    private final class InnerPostStep<M, T> implements PostStep<M, T> {

      private final ExceptionalFunction<ContextGiven, M> supplier;

      private final ExceptionalBiFunction<M, ContextGiven, T> method;

      private final List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers = new ArrayList<>();

      private InnerPostStep(
          ExceptionalFunction<ContextGiven, M> supplier,
          ExceptionalBiFunction<M, ContextGiven, T> method,
          ExceptionalFunction<ContextGiven, Answer<Object>> answer) {
        this.supplier = supplier;
        this.method = method;
        this.answers.add(answer);
      }

      @Override
      public PostStep<M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response) {
        this.answers.add(ctx -> i -> requireNonNull(response.apply(i, ctx)));
        return this;
      }

      @Override
      public PostStep<M, T> willReturn(ExceptionalFunction<ContextGiven, T> response) {
        this.answers.add(ctx -> new Returns(response.apply(ctx)));
        return this;
      }

      @Override
      public PostStep<M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response) {
        this.answers.add(ctx -> new ThrowsException(response.apply(ctx)));
        return this;
      }

      @Override
      public <T2> ResponseStep<M, T2> invoking(ExceptionalBiFunction<M, ContextGiven, T2> method) {
        return new InnerResponseStep<>(buildSupplier(), method);
      }

      @Override
      public SetupBuilder.ExtensionStep given(String variable) {
        return builder
            .metadata(MockitoMetadata.Variable.IS_MOCK, true)
            .is(buildSupplier())
            .given(variable);
      }

      private ExceptionalFunction<ContextGiven, M> buildSupplier() {
        return ctx -> {
          try {
            var mock = requireNonNull(supplier.apply(ctx));
            var answerIterator = answers.iterator();
            var stubber = Mockito.lenient().doAnswer(answerIterator.next().apply(ctx));
            while (answerIterator.hasNext()) {
              answerIterator.next().apply(ctx);
            }
            method.apply(stubber.when(mock), ctx);
            return mock;
          } catch (Exception e) {
            throw new MockitoExtensionException("Caught exception", e); // TODO Add better message
          }
        };
      }
    }
  }
}
