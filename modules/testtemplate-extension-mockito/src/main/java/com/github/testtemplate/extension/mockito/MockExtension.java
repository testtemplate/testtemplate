package com.github.testtemplate.extension.mockito;

import com.github.testtemplate.AlternativeTestTemplateExceptBuilder;
import com.github.testtemplate.Context;
import com.github.testtemplate.ContextView;
import com.github.testtemplate.ContextualTemplate;
import com.github.testtemplate.ContextualValidator;
import com.github.testtemplate.DefaultTestTemplateGivenBuilder;
import com.github.testtemplate.DefaultTestValidatorBuilder;
import com.github.testtemplate.TestTemplateBuilder;

import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class MockExtension<S, R>
    implements DefaultTestTemplateGivenBuilder.ExtensionFactory<S, MockGivenBuilder<S>>,
        AlternativeTestTemplateExceptBuilder.ExtensionFactory<S, R, MockExceptInvokeBuilder<S, R>> {

  @Override
  public MockGivenBuilder<S> getExtension(DefaultTestTemplateGivenBuilder<S> builder, String variable) {
    return new InnerMockGivenBuilder<>(builder, variable);
  }

  @Override
  public MockExceptInvokeBuilder<S, R> getExtension(
      AlternativeTestTemplateExceptBuilder<S, R> builder,
      String variable) {
    return new InnerMockExceptBuilder<>(builder, variable);
  }

  private static final class InnerMockGivenBuilder<S> implements MockGivenBuilder<S> {

    private final DefaultTestTemplateGivenBuilder<S> builder;

    private final String variable;

    private InnerMockGivenBuilder(DefaultTestTemplateGivenBuilder<S> builder, String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    @Override
    public <M> MockGivenInvokeBuilder<S, M> mock(Class<? extends M> classToMock) {
      return new InnerMockGivenInvokeBuilder<>(
          builder,
          c -> Mockito.mock(classToMock, variable));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> MockGivenInvokeBuilder<S, M> use(M mock) {
      if (!Mockito.mockingDetails(mock).isMock()) {
        throw new IllegalArgumentException("The object must be a mock (or a spy)");
      }

      return new InnerMockGivenInvokeBuilder<>(builder, c -> {
        Mockito.reset(mock);
        return mock;
      });
    }
  }

  private static final class InnerMockGivenInvokeBuilder<S, M> implements MockGivenInvokeBuilder<S, M> {

    private final DefaultTestTemplateGivenBuilder<S> builder;

    private final Function<Context, M> supplier;

    private InnerMockGivenInvokeBuilder(
        DefaultTestTemplateGivenBuilder<S> builder,
        Function<Context, M> supplier) {
      this.builder = builder;
      this.supplier = supplier;
    }

    @Override
    public <T> MockGivenResponseBuilder<S, M, T> invoking(BiFunction<M, Context, T> method) {
      return new InnerMockGivenResponseBuilder<>(builder, supplier, method);
    }

    @Override
    public DefaultTestTemplateGivenBuilder<S> given(String variable) {
      return builder.preload().is(supplier).given(variable);
    }

    @Override
    public <R> DefaultTestValidatorBuilder<S, R> when(ContextualTemplate<R> template) {
      return builder.preload().is(supplier).when(template);
    }
  }

  private static final class InnerMockGivenResponseBuilder<S, M, T> implements MockGivenResponseBuilder<S, M, T> {

    private final DefaultTestTemplateGivenBuilder<S> builder;

    private final Function<Context, M> supplier;

    private final BiFunction<M, Context, T> method;

    private InnerMockGivenResponseBuilder(
        DefaultTestTemplateGivenBuilder<S> builder,
        Function<Context, M> supplier,
        BiFunction<M, Context, T> method) {
      this.builder = builder;
      this.supplier = supplier;
      this.method = method;
    }

    @Override
    public MockGivenPostResponseBuilder<S, M, T> willAnswer(ContextualAnswer<T> response) {
      return new InnerMockGivenPostResponseBuilder<>(builder, supplier, method, c -> i -> response.answer(i, c));
    }

    @Override
    public MockGivenPostResponseBuilder<S, M, T> willReturn(Function<Context, T> response) {
      return new InnerMockGivenPostResponseBuilder<>(
          builder,
          supplier,
          method,
          c -> new Returns(response.apply(c)));
    }

    @Override
    public MockGivenPostResponseBuilder<S, M, T> willThrow(Function<Context, Throwable> response) {
      return new InnerMockGivenPostResponseBuilder<>(
          builder,
          supplier,
          method,
          c -> new ThrowsException(response.apply(c)));
    }
  }

  private static final class InnerMockGivenPostResponseBuilder<S, M, T>
      implements MockGivenPostResponseBuilder<S, M, T> {

    private final DefaultTestTemplateGivenBuilder<S> builder;

    private final Function<Context, M> supplier;

    private final BiFunction<M, Context, T> method;

    private final List<Function<Context, Answer<Object>>> answers = new ArrayList<>();

    private InnerMockGivenPostResponseBuilder(
        DefaultTestTemplateGivenBuilder<S> builder,
        Function<Context, M> supplier,
        BiFunction<M, Context, T> method,
        Function<Context, Answer<Object>> answer) {
      this.builder = builder;
      this.supplier = supplier;
      this.method = method;
      this.answers.add(answer);
    }

    @Override
    public MockGivenPostResponseBuilder<S, M, T> willAnswer(MockGivenResponseBuilder.ContextualAnswer<T> response) {
      answers.add(c -> i -> response.answer(i, c));
      return this;
    }

    @Override
    public MockGivenPostResponseBuilder<S, M, T> willReturn(Function<Context, T> response) {
      answers.add(c -> new Returns(response.apply(c)));
      return this;
    }

    @Override
    public MockGivenPostResponseBuilder<S, M, T> willThrow(Function<Context, Throwable> response) {
      answers.add(c -> new ThrowsException(response.apply(c)));
      return this;
    }

    @Override
    public <T2> MockGivenResponseBuilder<S, M, T2> invoking(BiFunction<M, Context, T2> anotherMethod) {
      return new InnerMockGivenResponseBuilder<>(builder, buildSupplier(), anotherMethod);
    }

    @Override
    public DefaultTestTemplateGivenBuilder<S> given(String variable) {
      return builder.preload().is(buildSupplier()).given(variable);
    }

    @Override
    public <R> DefaultTestValidatorBuilder<S, R> when(ContextualTemplate<R> template) {
      return builder.preload().is(buildSupplier()).when(template);
    }

    private Function<Context, M> buildSupplier() {
      return c -> {
        var mock = supplier.apply(c);
        var answerIterator = answers.iterator();
        var stubber = Mockito.lenient().doAnswer(answerIterator.next().apply(c));
        answerIterator.forEachRemaining(answers -> stubber.doAnswer(answers.apply(c)));
        method.apply(stubber.when(mock), c);
        return mock;
      };
    }
  }

  private static final class InnerMockExceptBuilder<S, R> implements MockExceptInvokeBuilder<S, R> {

    private final AlternativeTestTemplateExceptBuilder<S, R> builder;

    private final String variable;

    private InnerMockExceptBuilder(AlternativeTestTemplateExceptBuilder<S, R> builder, String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    @Override
    public <M, T> MockExpectResponseBuilder<S, R, M, T> invoking(BiFunction<M, ContextView, T> method) {
      return new InnerMockExceptResponseBuilder<>(builder, variable, method);
    }
  }

  private static final class InnerMockExceptResponseBuilder<S, R, M, T>
      implements MockExpectResponseBuilder<S, R, M, T> {

    private final AlternativeTestTemplateExceptBuilder<S, R> builder;

    private final String variable;

    private final BiFunction<M, ContextView, T> method;

    private InnerMockExceptResponseBuilder(
        AlternativeTestTemplateExceptBuilder<S, R> builder,
        String variable,
        BiFunction<M, ContextView, T> method) {
      this.builder = builder;
      this.variable = variable;
      this.method = method;
    }

    @Override
    public MockExpectPostResponseBuilder<S, R, M, T> willAnswer(ContextualAnswer<T> response) {
      return new InnerMockExceptPostResponseBuilder<>(builder, variable, method, c -> i -> response.answer(i, c));
    }

    @Override
    public MockExpectPostResponseBuilder<S, R, M, T> willReturn(Function<ContextView, T> response) {
      return new InnerMockExceptPostResponseBuilder<>(
          builder,
          variable,
          method,
          c -> new Returns(response.apply(c)));
    }

    @Override
    public MockExpectPostResponseBuilder<S, R, M, T> willThrow(Function<ContextView, Throwable> response) {
      return new InnerMockExceptPostResponseBuilder<>(
          builder,
          variable,
          method,
          c -> new ThrowsException(response.apply(c)));
    }
  }

  private static final class InnerMockExceptPostResponseBuilder<S, R, M, T>
      implements MockExpectPostResponseBuilder<S, R, M, T> {

    private final AlternativeTestTemplateExceptBuilder<S, R> builder;

    private final String variable;

    private final BiFunction<M, ContextView, T> method;

    private final List<Function<ContextView, Answer<Object>>> answers = new ArrayList<>();

    private InnerMockExceptPostResponseBuilder(
        AlternativeTestTemplateExceptBuilder<S, R> builder,
        String variable,
        BiFunction<M, ContextView, T> method,
        Function<ContextView, Answer<Object>> answer) {
      this.builder = builder;
      this.variable = variable;
      this.method = method;
      this.answers.add(answer);
    }

    @Override
    public MockExpectPostResponseBuilder<S, R, M, T> willAnswer(ContextualAnswer<T> response) {
      answers.add(c -> i -> response.answer(i, c));
      return this;
    }

    @Override
    public MockExpectPostResponseBuilder<S, R, M, T> willReturn(Function<ContextView, T> response) {
      answers.add(c -> new Returns(response.apply(c)));
      return this;
    }

    @Override
    public MockExpectPostResponseBuilder<S, R, M, T> willThrow(Function<ContextView, Throwable> response) {
      answers.add(c -> new ThrowsException(response.apply(c)));
      return this;
    }

    @Override
    public AlternativeTestTemplateExceptBuilder<S, R> except(String variable) {
      return builder.is(buildSupplier()).except(variable);
    }

    @Override
    public TestTemplateBuilder<S, R> then(ContextualValidator<R> validator) {
      return builder.is(buildSupplier()).then(validator);
    }

    private Function<ContextView, M> buildSupplier() {
      return c -> {
        M mock = c.get(variable);
        var answerIterator = answers.iterator();
        var stubber = Mockito.lenient().doAnswer(answerIterator.next().apply(c));
        answerIterator.forEachRemaining(answers -> stubber.doAnswer(answers.apply(c)));
        method.apply(stubber.when(mock), c);
        return mock;
      };
    }
  }
}
