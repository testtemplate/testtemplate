package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.function.ExceptionalConsumer;
import io.github.testtemplate.api.function.ExceptionalFunction;

import org.jspecify.annotations.Nullable;

final class TestRunner<R> {

  private final ExceptionalFunction<ContextGiven, R> template;

  private final  ExceptionalConsumer<ContextResult<R>> validator;

  private final RunnerVariableResolver variableResolver;

  private Listener<R> listener = new NoOpListener<>();

  TestRunner(
      ExceptionalFunction<ContextGiven, R> template,
      ExceptionalConsumer<ContextResult<R>> validator,
      RunnerVariableResolver variableResolver) {
    this.template = template;
    this.validator = validator;
    this.variableResolver = variableResolver;
  }

  public void register(@Nullable Listener<R> listener) {
    this.listener = listener != null ? listener : new NoOpListener<>();
  }

  public void execute() {

    listener.before();
    try {
      RunnerContextResult<R> contextResult;
      try {
        RunnerContextGiven context = new RunnerContextGiven(variableResolver);
        R result = template.apply(context);
        listener.result(result);
        contextResult = new RunnerContextResult<>(variableResolver, result);
      } catch (TestRunnerException exception) {
        throw exception;
      } catch (Exception exception) {
        listener.exception(exception);
        contextResult = new RunnerContextResult<>(variableResolver, exception);
      }

      validator.accept(contextResult);
      contextResult.doubleCheck();
    } catch (TestRunnerException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new TestRunnerException("The test has thrown an unexpected exception", exception);
    } finally {
      listener.after();
    }
  }

  interface Listener<R> {

    void before();

    void after();

    void result(@Nullable R result);

    void exception(Throwable exception);

  }

  private static final class NoOpListener<R> implements Listener<R> {

    @Override
    public void before() {}

    @Override
    public void after() {}

    @Override
    public void exception(Throwable exception) {}

    @Override
    public void result(@Nullable R result) {}
  }
}
