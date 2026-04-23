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

  private static <M, C extends Context> ExceptionalFunction<C, M> buildSupplier(
      ExceptionalFunction<C, M> mockSupplier,
      List<InvocationSupplier<M, C>> invocationSuppliers) {

    return ctx -> {
      try {
        var mock = requireNonNull(mockSupplier.apply(ctx));

        for (var invocationSupplier : invocationSuppliers) {
          var answerIterator = invocationSupplier.getAnswers().iterator();
          var stubber = Mockito.lenient().doAnswer(answerIterator.next().apply(ctx));
          while (answerIterator.hasNext()) {
            stubber = stubber.doAnswer(answerIterator.next().apply(ctx));
          }
          invocationSupplier.getMethod().apply(stubber.when(mock), ctx);
        }

        return mock;
      } catch (Exception e) {
        throw new MockitoExtensionException("Caught exception", e); // TODO Add better message
      }
    };
  }

  private static final class InvocationSupplier<M, C extends Context> {

    private final ExceptionalBiFunction<M, C, ?> method;

    private final List<ExceptionalFunction<C, Answer<Object>>> answers = new ArrayList<>();

    private InvocationSupplier(ExceptionalBiFunction<M, C, ?> method) {
      this.method = method;
    }

    public ExceptionalBiFunction<M, C, ?> getMethod() {
      return method;
    }

    public List<ExceptionalFunction<C, Answer<Object>>> getAnswers() {
      return answers;
    }
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
      var invocationSuppliers = new ArrayList<InvocationSupplier<M, ContextGiven>>();
      var next = builder
          .metadata(MockitoMetadata.Variable.IS_MOCK, true)
          .is(buildSupplier(ctx -> Mockito.mock(classToMock, variable), invocationSuppliers));
      return new InnerInvokingStep<>(next, invocationSuppliers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> InvokingStep<S, M> use(M mock) {
      if (!Mockito.mockingDetails(mock).isMock()) {
        throw new MockitoExtensionException("The object must be a mock (or a spy)");
      }

      var invocationSuppliers = new ArrayList<InvocationSupplier<M, ContextGiven>>();
      var next = builder
          .metadata(MockitoMetadata.Variable.IS_MOCK, true)
          .is(buildSupplier(
              ctx -> {
                Mockito.reset(mock);
                return mock;
              },
              invocationSuppliers));
      return new InnerInvokingStep<>(next, invocationSuppliers);
    }

    private static final class InnerInvokingStep<S, M> implements InvokingStep<S, M> {

      private final DefaultBuilder.GivenStep<S> next;

      private final List<InvocationSupplier<M, ContextGiven>> invocationSuppliers;

      private InnerInvokingStep(
          DefaultBuilder.GivenStep<S> next,
          List<InvocationSupplier<M, ContextGiven>> invocationSuppliers) {
        this.next = next;
        this.invocationSuppliers = invocationSuppliers;
      }

      @Override
      public <T> ResponseStep<S, M, T> invoking(ExceptionalBiFunction<M, ContextGiven, T> method) {
        var invocationSupplier = new InvocationSupplier<>(method);
        invocationSuppliers.add(invocationSupplier);
        return new InnerResponseStep<>(next, this, invocationSupplier.getAnswers());
      }

      @Override
      public ExtensionStep<S> given(String variable) {
        return next.given(variable);
      }

      @Override
      public <R> DefaultBuilder.ThenStep<S, R> when(ExceptionalFunction<ContextGiven, R> template) {
        return next.when(template);
      }
    }

    private static final class InnerResponseStep<S, M, T> implements ResponseStep<S, M, T> {

      private final DefaultBuilder.GivenStep<S> next;

      private final InnerInvokingStep<S, M> invokingStep;

      private final List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers;

      private InnerResponseStep(
          DefaultBuilder.GivenStep<S> next,
          InnerInvokingStep<S, M> invokingStep,
          List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers) {
        this.next = next;
        this.invokingStep = invokingStep;
        this.answers = answers;
      }

      @Override
      public PostStep<S, M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response) {
        answers.add(ctx -> i -> requireNonNull(response.apply(i, ctx)));
        return new InnerPostStep<>(next, invokingStep, answers);
      }

      @Override
      public PostStep<S, M, T> willReturn(ExceptionalFunction<ContextGiven, T> response) {
        answers.add(ctx -> new Returns(response.apply(ctx)));
        return new InnerPostStep<>(next, invokingStep, answers);
      }

      @Override
      public PostStep<S, M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response) {
        answers.add(ctx -> new ThrowsException(response.apply(ctx)));
        return new InnerPostStep<>(next, invokingStep, answers);
      }
    }

    private static final class InnerPostStep<S, M, T> implements PostStep<S, M, T> {

      private final DefaultBuilder.GivenStep<S> next;

      private final InnerInvokingStep<S, M> invokingStep;

      private final List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers;

      private InnerPostStep(
          DefaultBuilder.GivenStep<S> next,
          InnerInvokingStep<S, M> invokingStep,
          List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers) {
        this.next = next;
        this.invokingStep = invokingStep;
        this.answers = answers;
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
        return invokingStep.invoking(method);
      }

      @Override
      public ExtensionStep<S> given(String variable) {
        return next.given(variable);
      }

      @Override
      public <R> DefaultBuilder.ThenStep<S, R> when(ExceptionalFunction<ContextGiven, R> template) {
        return next.when(template);
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
      var invocationSuppliers = new ArrayList<InvocationSupplier<M, Context>>();
      var next = builder
          .metadata(MockitoMetadata.Variable.IS_MOCK, true)
          .is(buildSupplier(ctx -> ctx.get(variable), invocationSuppliers));
      var invocationSupplier = new InvocationSupplier<>(method);
      invocationSuppliers.add(invocationSupplier);
      return new InnerResponseStep<>(next, invocationSupplier.getAnswers());
    }

    private static final class InnerResponseStep<S, R, M, T> implements ResponseStep<S, R, M, T> {

      private final AlternativeBuilder.ExceptStep.PostStep<S, R> next;

      private final List<ExceptionalFunction<Context, Answer<Object>>> answers;

      private InnerResponseStep(
          AlternativeBuilder.ExceptStep.PostStep<S, R> next,
          List<ExceptionalFunction<Context, Answer<Object>>> answers) {
        this.next = next;
        this.answers = answers;
      }

      @Override
      public PostStep<S, R, M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, Context, T> response) {
        answers.add(ctx -> i -> requireNonNull(response.apply(i, ctx)));
        return new InnerPostStep<>(next, answers);
      }

      @Override
      public PostStep<S, R, M, T> willReturn(ExceptionalFunction<Context, T> response) {
        answers.add(ctx -> new Returns(response.apply(ctx)));
        return new InnerPostStep<>(next, answers);
      }

      @Override
      public PostStep<S, R, M, T> willThrow(ExceptionalFunction<Context, Throwable> response) {
        answers.add(ctx -> new ThrowsException(response.apply(ctx)));
        return new InnerPostStep<>(next, answers);
      }
    }

    private static final class InnerPostStep<S, R, M, T> implements PostStep<S, R, M, T> {

      private final AlternativeBuilder.ExceptStep.PostStep<S, R> next;

      private final List<ExceptionalFunction<Context, Answer<Object>>> answers;

      private InnerPostStep(
          AlternativeBuilder.ExceptStep.PostStep<S, R> next,
          List<ExceptionalFunction<Context, Answer<Object>>> answers) {
        this.next = next;
        this.answers = answers;
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
      public ExtensionStep<S, R> except(String variable) {
        return next.except(variable);
      }

      @Override
      public Value2Step<S, R> except(String variable1, String variable2) {
        return next.except(variable1, variable2);
      }

      @Override
      public Value3Step<S, R> except(String variable1, String variable2, String variable3) {
        return next.except(variable1, variable2, variable3);
      }

      @Override
      public Value4Step<S, R> except(
          String variable1,
          String variable2,
          String variable3,
          String variable4) {
        return next.except(variable1, variable2, variable3, variable4);
      }

      @Override
      public Value5Step<S, R> except(
          String variable1,
          String variable2,
          String variable3,
          String variable4,
          String variable5) {
        return next.except(variable1, variable2, variable3, variable4, variable5);
      }

      @Override
      public ValueNStep<S, R> except(List<String> variables) {
        return next.except(variables);
      }

      @Override
      public AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator) {
        return next.then(validator);
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
      var invocationSuppliers = new ArrayList<InvocationSupplier<M, ContextGiven>>();
      var next = builder.is(buildSupplier(
          ctx -> Mockito.mock(classToMock, variable), invocationSuppliers));
      return new InnerInvokingStep<>(next, invocationSuppliers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> InvokingStep<M> use(M mock) {
      if (!Mockito.mockingDetails(mock).isMock()) {
        throw new MockitoExtensionException("The object must be a mock (or a spy)");
      }

      // Be sure there is no stubbing outside the test builder because we will reset the mock after each test.
      Mockito.reset(mock);

      var invocationSuppliers = new ArrayList<InvocationSupplier<M, ContextGiven>>();
      var next = builder.is(buildSupplier(ctx -> mock, invocationSuppliers));
      return new InnerInvokingStep<>(next, invocationSuppliers);
    }

    private static final class InnerInvokingStep<M> implements InvokingStep<M> {

      private final SetupBuilder.GivenStep next;

      private final List<InvocationSupplier<M, ContextGiven>> invocationSuppliers;

      private InnerInvokingStep(
          SetupBuilder.GivenStep next,
          List<InvocationSupplier<M, ContextGiven>> invocationSuppliers) {
        this.next = next;
        this.invocationSuppliers = invocationSuppliers;
      }

      @Override
      public <T> ResponseStep<M, T> invoking(ExceptionalBiFunction<M, ContextGiven, T> method) {
        var invocationSupplier = new InvocationSupplier<>(method);
        invocationSuppliers.add(invocationSupplier);
        return new InnerResponseStep<>(next, this, invocationSupplier.getAnswers());
      }

      @Override
      public SetupBuilder.ExtensionStep given(String variable) {
        return next.given(variable);
      }
    }

    private static final class InnerResponseStep<M, T> implements ResponseStep<M, T> {

      private final SetupBuilder.GivenStep next;

      private final InnerInvokingStep<M> invokingStep;

      private final List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers;

      private InnerResponseStep(
          SetupBuilder.GivenStep next,
          InnerInvokingStep<M> invokingStep,
          List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers) {
        this.next = next;
        this.invokingStep = invokingStep;
        this.answers = answers;
      }

      @Override
      public PostStep<M, T> willAnswer(ExceptionalBiFunction<InvocationOnMock, ContextGiven, T> response) {
        answers.add(ctx -> i -> requireNonNull(response.apply(i, ctx)));
        return new InnerPostStep<>(next, invokingStep, answers);
      }

      @Override
      public PostStep<M, T> willReturn(ExceptionalFunction<ContextGiven, T> response) {
        answers.add(ctx -> new Returns(response.apply(ctx)));
        return new InnerPostStep<>(next, invokingStep, answers);
      }

      @Override
      public PostStep<M, T> willThrow(ExceptionalFunction<ContextGiven, Throwable> response) {
        answers.add(ctx -> new ThrowsException(response.apply(ctx)));
        return new InnerPostStep<>(next, invokingStep, answers);
      }
    }

    private static final class InnerPostStep<M, T> implements PostStep<M, T> {

      private final SetupBuilder.GivenStep next;

      private final InnerInvokingStep<M> invokingStep;

      private final List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers;

      private InnerPostStep(
          SetupBuilder.GivenStep next,
          InnerInvokingStep<M> invokingStep,
          List<ExceptionalFunction<ContextGiven, Answer<Object>>> answers) {
        this.next = next;
        this.invokingStep = invokingStep;
        this.answers = answers;
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
        return invokingStep.invoking(method);
      }

      @Override
      public SetupBuilder.ExtensionStep given(String variable) {
        return next.given(variable);
      }
    }
  }
}
