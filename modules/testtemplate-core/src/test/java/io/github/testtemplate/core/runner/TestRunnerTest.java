package io.github.testtemplate.core.runner;

import io.github.testtemplate.ContextualTemplate;
import io.github.testtemplate.ContextualValidator;
import io.github.testtemplate.TestListener;
import io.github.testtemplate.TestSuiteFactory;
import io.github.testtemplate.core.TestDefinition;
import io.github.testtemplate.core.TestModifier;
import io.github.testtemplate.core.TestParameter;
import io.github.testtemplate.core.TestVariable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.testtemplate.TestListener.VariableType.ORIGINAL;
import static io.github.testtemplate.TestType.DEFAULT;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class TestRunnerTest {

  private static final String TEST_NAME = "test-name";
  private static final ContextualTemplate<Object> NO_OP_TEMPLATE = c -> null;
  private static final Set<TestVariable> EMPTY_VARIABLE_SET = Set.of();
  private static final Set<TestModifier> EMPTY_MODIFIER_SET = Set.of();
  private static final Set<TestParameter> EMPTY_PARAMETER_SET = Set.of();
  private static final ContextualValidator<Object> NO_OP_VALIDATOR = c -> {};
  private static final Map<String, Object> EMPTY_ATTRIBUTE_MAP = Map.of();

  @Nested
  class RunTest {

    private final TestRunner runner = new TestRunner(emptyList());

    @Test
    void shouldSilentlyReturnWhenResultIsTheExpectedOne() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> "This is the result of the test",
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          c -> assertThat(c.result()).isEqualTo("This is the result of the test"),
          EMPTY_ATTRIBUTE_MAP);

      ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute();
    }

    @Test
    void shouldThrowExceptionWhenResultIsNotTheExceptedOne() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> "This is the result of the test",
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          c -> assertThat(c.result()).isEqualTo("But we expect something else"),
          EMPTY_ATTRIBUTE_MAP);

      Assertions
          .assertThatThrownBy(() -> ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute())
          .isInstanceOf(AssertionFailedError.class)
          .hasMessageContaining("But we expect something else");
    }

    @Test
    void shouldSilentlyReturnWhenExceptionIsTheExpectedOne() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> { throw new Exception("This is expected"); },
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          c -> assertThat(c.exception()).isInstanceOf(Exception.class).hasMessage("This is expected"),
          EMPTY_ATTRIBUTE_MAP);

      ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute();
    }

    @Test
    void shouldThrowExceptionWhenExceptionIsNotTheExceptedOne() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> { throw new Exception("This is an exception"); },
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          c -> assertThat(c.exception()).isInstanceOf(Exception.class).hasMessage("But expect another one"),
          EMPTY_ATTRIBUTE_MAP);

      Assertions
          .assertThatThrownBy(() -> ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute())
          .isInstanceOf(AssertionFailedError.class)
          .hasMessageContaining("But expect another one");
    }

    @Test
    void shouldNotValidateTestRunException() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> { throw new TestRunnerException("This is for internal purpose"); },
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          c -> { throw new RuntimeException("Not expected"); },
          EMPTY_ATTRIBUTE_MAP);

      Assertions
          .assertThatThrownBy(() -> ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute())
          .isInstanceOf(TestRunnerException.class)
          .hasMessage("This is for internal purpose");
    }
  }

  @Nested
  class ListenerTest {

    private final TestListener listener = Mockito.mock(TestListener.class);

    private final TestRunner runner = new TestRunner(List.of(listener));

    @Test
    void shouldBeInvokedBeforeTest() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          NO_OP_TEMPLATE,
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          NO_OP_VALIDATOR,
          EMPTY_ATTRIBUTE_MAP);

      ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute();

      var context = ArgumentCaptor.forClass(TestListener.Test.class);
      Mockito.verify(listener).before(context.capture());

      assertThat(context.getValue())
          .hasFieldOrPropertyWithValue("name", TEST_NAME)
          .hasFieldOrPropertyWithValue("type", DEFAULT);
    }

    @Test
    void shouldBeInvokedAfterTest() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          NO_OP_TEMPLATE,
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          NO_OP_VALIDATOR,
          EMPTY_ATTRIBUTE_MAP);

      ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute();

      var context = ArgumentCaptor.forClass(TestListener.Test.class);
      Mockito.verify(listener).after(context.capture());

      assertThat(context.getValue())
          .hasFieldOrPropertyWithValue("name", TEST_NAME)
          .hasFieldOrPropertyWithValue("type", DEFAULT);
    }

    @Test
    void shouldAlwaysBeInvokedAfterTest() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          NO_OP_TEMPLATE,
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          c -> fail(),
          EMPTY_ATTRIBUTE_MAP);

      Assertions
          .assertThatThrownBy(() -> ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute())
          .isInstanceOf(AssertionFailedError.class);

      var context = ArgumentCaptor.forClass(TestListener.Test.class);
      Mockito.verify(listener).after(context.capture());

      assertThat(context.getValue())
          .hasFieldOrPropertyWithValue("name", TEST_NAME)
          .hasFieldOrPropertyWithValue("type", DEFAULT);
    }

    @Test
    void shouldNotCatchException() {
      Mockito.doThrow(TestAbortedException.class).when(listener).before(Mockito.any());

      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          NO_OP_TEMPLATE,
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          NO_OP_VALIDATOR,
          EMPTY_ATTRIBUTE_MAP);

      Assertions
          .assertThatThrownBy(() -> ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute())
          .isInstanceOf(TestAbortedException.class);

      Mockito.verify(listener, Mockito.never()).after(Mockito.any());
    }

    @Test
    void shouldBeInvokedWithResult() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> "welcome",
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          NO_OP_VALIDATOR,
          EMPTY_ATTRIBUTE_MAP);

      ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute();

      var context = ArgumentCaptor.forClass(TestListener.Test.class);
      Mockito.verify(listener).result(context.capture(), Mockito.eq("welcome"));
      Mockito.verify(listener, Mockito.never()).exception(Mockito.any(), Mockito.any());

      assertThat(context.getValue())
          .hasFieldOrPropertyWithValue("name", TEST_NAME)
          .hasFieldOrPropertyWithValue("type", DEFAULT);
    }

    @Test
    void shouldBeInvokedWithException() {
      var exception = new Exception("oops");

      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> { throw exception; },
          EMPTY_VARIABLE_SET,
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          NO_OP_VALIDATOR,
          EMPTY_ATTRIBUTE_MAP);

      ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute();

      var context = ArgumentCaptor.forClass(TestListener.Test.class);
      Mockito.verify(listener, Mockito.never()).result(Mockito.any(), Mockito.any());
      Mockito.verify(listener).exception(context.capture(), Mockito.eq(exception));

      assertThat(context.getValue())
          .hasFieldOrPropertyWithValue("name", TEST_NAME)
          .hasFieldOrPropertyWithValue("type", DEFAULT);
    }

    @Test
    void shouldBeInvokedWithVariable() {
      var test = new TestDefinition<>(
          TEST_NAME,
          DEFAULT,
          c -> c.get("greeting"),
          Set.of(new TestVariable("greeting", c -> "welcome")),
          EMPTY_MODIFIER_SET,
          EMPTY_PARAMETER_SET,
          NO_OP_VALIDATOR,
          EMPTY_ATTRIBUTE_MAP);

      ((TestSuiteFactory.TestItem) runner.toInstance(test)).execute();

      var context = ArgumentCaptor.forClass(TestListener.Test.class);
      Mockito
          .verify(listener)
          .variable(context.capture(), Mockito.eq("greeting"), Mockito.eq(ORIGINAL), Mockito.eq("welcome"));
    }
  }
}
