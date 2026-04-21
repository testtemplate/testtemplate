package io.github.testtemplate.core.runner;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;

import static io.github.testtemplate.api.VariableType.MODIFIED;
import static io.github.testtemplate.api.VariableType.ORIGINAL;

@ExtendWith(MockitoExtension.class)
class TestRunnerTest {

  @Mock
  private RunnerVariableResolver resolver;

  private final RunnerVariableDescriptor valueDescriptor = new RunnerVariableDescriptor();

  @Test
  void shouldSilentlyReturnWhenResultIsTheExpectedOne() {
    var testRunner = new TestRunner<>(
        ctx -> "This is the result of the test",
        ctx -> Assertions.assertThat(ctx.result()).isEqualTo("This is the result of the test"),
        resolver);
    testRunner.execute();
  }

  @Test
  void shouldThrowExceptionWhenResultIsNotTheExceptedOne() {
    var testRunner = new TestRunner<>(
        ctx -> "This is the result of the test",
        ctx -> Assertions.assertThat(ctx.result()).isEqualTo("But we expect something else"),
        resolver);

    Assertions
        .assertThatThrownBy(testRunner::execute)
        .isInstanceOf(AssertionFailedError.class)
        .hasMessageContaining("But we expect something else");
  }

  @Test
  void shouldThrowAssertionErrorWhenAnExceptionIsThrown() {
    var testRunner = new TestRunner<>(
        ctx -> { throw new Exception("The invocation throws an exception"); },
        ctx -> Assertions.assertThat(ctx.result()).isEqualTo("This is the result of the test"),
        resolver);

    Assertions
        .assertThatThrownBy(testRunner::execute)
        .isInstanceOf(AssertionFailedError.class)
        .hasMessage("The test expects a result but an exception was thrown");
  }

  @Test
  void shouldSilentlyReturnWhenExceptionIsTheExpectedOne() {
    var testRunner = new TestRunner<>(
        ctx -> { throw new Exception("This is expected"); },
        ctx -> Assertions
            .assertThat(ctx.exception())
            .isInstanceOf(Exception.class)
            .hasMessage("This is expected"),
        resolver);

    testRunner.execute();
  }

  @Test
  void shouldThrowExceptionWhenExceptionIsNotTheExceptedOne() {
    var testRunner = new TestRunner<>(
        ctx -> { throw new Exception("This is an exception"); },
        ctx -> Assertions
            .assertThat(ctx.exception())
            .isInstanceOf(Exception.class)
            .hasMessage("But expect an another one"),
        resolver);

    Assertions
        .assertThatThrownBy(testRunner::execute)
        .isInstanceOf(AssertionFailedError.class)
        .hasMessageContaining("But expect an another one");
  }

  @Test
  void shouldThrowExceptionWhenNoExceptionIsThrown() {
    var testRunner = new TestRunner<>(
        ctx -> "This is the unexpected result of the test",
        ctx -> Assertions
            .assertThat(ctx.exception())
            .isInstanceOf(Exception.class)
            .hasMessage("But expect an exception"),
        resolver);

    Assertions
        .assertThatThrownBy(testRunner::execute)
        .isInstanceOf(AssertionFailedError.class)
        .hasMessage("The test expects an exception but no exception was thrown");
  }

  @Test
  void shouldSilentlyReturnWhenResultIsNotVerifiedButThereIsNoException() {
    var testRunner = new TestRunner<>(
        ctx -> "This is the result of the test",
        ctx -> { /* no check */ },
        resolver);

    testRunner.execute();
  }

  @Test
  void shouldThrowExceptionWhenResultIsNotVerifiedButThereIsAnException() {
    var testRunner = new TestRunner<>(
        ctx -> { throw new Exception("The invocation throws an exception"); },
        ctx -> { /* no check */ },
        resolver);

    Assertions
        .assertThatThrownBy(testRunner::execute)
        .isInstanceOf(AssertionFailedError.class)
        .hasMessage("The test expects a result but an exception was thrown");
  }

  @Test
  void shouldCallVariableResolverWhenVariableIsGetInContextGiven() {
    Mockito
        .doReturn(
            new RunnerVariable("test-variable", ORIGINAL, () -> "This is the result of the variable", valueDescriptor))
        .when(resolver)
        .getVariable("test-variable");
    var testRunner = new TestRunner<>(
        ctx -> ctx.get("test-variable"),
        ctx -> Assertions.assertThat(ctx.result()).isEqualTo("This is the result of the variable"),
        resolver);

    testRunner.execute();
  }

  @Test
  void shouldCallVariableResolverWhenVariableIsGivenInContextGiven() {
    Mockito
        .doReturn(
            new RunnerVariable("test-variable", MODIFIED, () -> "This is the result of the variable", valueDescriptor))
        .when(resolver)
        .getVariableOrDefault("test-variable", "default value");
    TestRunner<String> testRunner = new TestRunner<>(
        ctx -> ctx.given("test-variable").is("default value"),
        ctx -> Assertions.assertThat(ctx.result()).isEqualTo("This is the result of the variable"),
        resolver);

    testRunner.execute();
  }

  @Test
  void shouldCallVariableResolverWhenVariableIsGetInContextResult() {
    Mockito
        .doReturn(new RunnerVariable("expectation", ORIGINAL, () -> "This is the result of the test", valueDescriptor))
        .when(resolver)
        .getVariable("expectation");
    TestRunner<String> testRunner = new TestRunner<>(
        ctx -> "This is the result of the test",
        ctx -> Assertions
            .assertThat(ctx.result())
            .isEqualTo(ctx.get("expectation")),
        resolver);

    testRunner.execute();
  }

  @Test
  void shouldSilentlyReturnWhenResultIsAnException() {
    TestRunner<Exception> testRunner = new TestRunner<>(
        ctx -> new Exception("This is ok"),
        ctx -> Assertions
            .assertThat(ctx.result())
            .isInstanceOf(Exception.class)
            .hasMessage("This is ok"),
        resolver);

    testRunner.execute();
  }

  @Nested
  class RegisterListenerTest {

    @Test
    void shouldInvokeListenerCallbacksOnSuccessfulExecution() {
      TestRunner.Listener<String> listener = Mockito.mock();
      var testRunner = new TestRunner<>(
          ctx -> "result",
          ctx -> {},
          resolver);
      testRunner.register(listener);

      testRunner.execute();

      var inOrder = Mockito.inOrder(listener);
      inOrder.verify(listener).before();
      inOrder.verify(listener).result("result");
      inOrder.verify(listener).after();
      Mockito.verify(listener, Mockito.never()).exception(Mockito.any());
    }

    @Test
    void shouldInvokeListenerCallbacksWhenTemplateThrows() {
      TestRunner.Listener<Object> listener = Mockito.mock();
      var cause = new Exception("boom");
      var testRunner = new TestRunner<>(
          ctx -> { throw cause; },
          ctx -> Assertions.assertThat(ctx.exception()).isSameAs(cause),
          resolver);
      testRunner.register(listener);

      testRunner.execute();

      var inOrder = Mockito.inOrder(listener);
      inOrder.verify(listener).before();
      inOrder.verify(listener).exception(cause);
      inOrder.verify(listener).after();
      Mockito.verify(listener, Mockito.never()).result(Mockito.any());
    }

    @Test
    void shouldFallBackToNoOpWhenNullListenerRegistered() {
      var testRunner = new TestRunner<>(
          ctx -> "result",
          ctx -> {},
          resolver);
      testRunner.register(null);

      Assertions.assertThatCode(testRunner::execute).doesNotThrowAnyException();
    }

    @Test
    void shouldRethrowTestRunnerExceptionFromTemplateUnwrapped() {
      var original = new TestRunnerException("internal error");
      var testRunner = new TestRunner<>(
          ctx -> { throw original; },
          ctx -> {},
          resolver);

      Assertions
          .assertThatThrownBy(testRunner::execute)
          .isSameAs(original);
    }

    @Test
    void shouldRethrowTestRunnerExceptionFromValidatorUnwrapped() {
      var original = new TestRunnerException("validation internal error");
      var testRunner = new TestRunner<>(
          ctx -> "result",
          ctx -> { throw original; },
          resolver);

      Assertions
          .assertThatThrownBy(testRunner::execute)
          .isSameAs(original);
    }

    @Test
    void shouldWrapUnexpectedValidatorExceptionInTestRunnerException() {
      var cause = new RuntimeException("unexpected");
      var testRunner = new TestRunner<>(
          ctx -> "result",
          ctx -> { throw cause; },
          resolver);

      Assertions
          .assertThatThrownBy(testRunner::execute)
          .isInstanceOf(TestRunnerException.class)
          .hasMessage("The test has thrown an unexpected exception")
          .hasCause(cause);
    }
  }
}
