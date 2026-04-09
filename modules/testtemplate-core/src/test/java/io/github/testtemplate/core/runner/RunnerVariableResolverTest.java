package io.github.testtemplate.core.runner;

import static io.github.testtemplate.api.VariableType.MODIFIED;
import static io.github.testtemplate.api.VariableType.ORIGINAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.testtemplate.api.Variable;
import io.github.testtemplate.core.TestModifier;
import io.github.testtemplate.core.TestVariable;

class RunnerVariableResolverTest {

  private final RunnerVariableDescriptor variableDescriptor = new RunnerVariableDescriptor();

  @Test
  void getVariableNamesShouldReturnMixOfVariablesAndModifiers() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", Map.of(), c -> "?"),
            new TestVariable("first-name", Map.of(), c -> "?"),
            new TestVariable("last-name", Map.of(), c -> "?")),
        Set.of(
            new TestModifier("greeting", Map.of(), c -> "?"),
            new TestModifier("another", Map.of(), c -> "?")),
        variableDescriptor);

    var names = variableResolver.getVariableNames();

    assertThat(names).containsExactlyInAnyOrder("greeting", "first-name", "last-name", "another");
  }

  @Test
  void getVariableShouldThrowExceptionWhenVariableIsUndefined() {
    var variableResolver = new RunnerVariableResolver(emptySet(), emptySet(), variableDescriptor);
    Assertions
        .assertThatThrownBy(() -> variableResolver.getVariable("greeting"))
        .isInstanceOf(TestRunnerException.class)
        .hasMessage("The variable 'greeting' is undefined");
  }

  @Test
  void getVariableShouldReturnOriginalValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", Map.of(), c -> "welcome")),
        emptySet(),
        variableDescriptor);

    var variable = variableResolver.getVariable("greeting");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome");
  }

  @Test
  void getVariableShouldReturnOverriddenValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", Map.of(), c -> "welcome")),
        Set.of(new TestModifier("greeting", Map.of(), c -> "hello")),
        variableDescriptor);

    var variable = variableResolver.getVariable("greeting");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", MODIFIED)
        .hasFieldOrPropertyWithValue("value", "hello");
  }

  @Test
  void getVariableShouldReturnModifiedValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", Map.of(), c -> "welcome")),
        Set.of(new TestModifier("greeting", Map.of(), c -> c.get("greeting") + " Bob")),
        variableDescriptor);

    var variable = variableResolver.getVariable("greeting");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", MODIFIED)
        .hasFieldOrPropertyWithValue("value", "welcome Bob");
  }

  @Test
  void getVariableShouldReturnComposedValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", Map.of(), c -> "welcome"),
            new TestVariable("name", Map.of(), c -> "Bob"),
            new TestVariable("message", Map.of(), c -> c.get("greeting") + " " + c.get("name"))),
        emptySet(),
        variableDescriptor);

    var variable = variableResolver.getVariable("message");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "message")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome Bob");
  }

  @Test
  void getVariableShouldReturnComposedValueWithOverriddenValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", Map.of(), c -> "welcome"),
            new TestVariable("name", Map.of(), c -> "Bob"),
            new TestVariable("message", Map.of(), c -> c.get("greeting") + " " + c.get("name"))),
        Set.of(
            new TestModifier("name", Map.of(), c -> "Alice")),
        variableDescriptor);

    var variable = variableResolver.getVariable("message");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "message")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome Alice");
  }

  @Test
  void getVariableShouldReturnNestedValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", Map.of(), c -> "welcome"),
            new TestVariable("message", Map.of(), c -> c.get("greeting") + " " + c.given("name").is("Bob"))),
        emptySet(),
        variableDescriptor);

    var variable = variableResolver.getVariable("message");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "message")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome Bob");
  }

  @Test
  void getVariableShouldReturnNestedValueWithOverriddenValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", Map.of(), c -> "welcome"),
            new TestVariable("message", Map.of(), c -> c.get("greeting") + " " + c.given("name").is("Bob"))),
        Set.of(
            new TestModifier("name", Map.of(), c -> "Alice")),
        variableDescriptor);

    var variable = variableResolver.getVariable("message");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "message")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome Alice");
  }

  @Test
  void getVariableShouldReturnMetadata() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", Map.of("test-key", "test-value"), c -> "welcome")),
        Set.of(new TestModifier("name", Map.of("other-key", "other-value"), c -> "Alice")),
        variableDescriptor);

    var var1 = variableResolver.getVariable("greeting");

    assertThat(var1.getMetadata("test-key")).isEqualTo("test-value");

    var var2 = variableResolver.getVariable("name");

    assertThat(var2.getMetadata("other-key")).isEqualTo("other-value");
  }

  @Test
  void getVariableShouldThrowExceptionWhenException() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", Map.of(), c -> {
              throw new Exception("catch me");
            })),
        emptySet(),
        variableDescriptor);

    Assertions
        .assertThatThrownBy(() -> variableResolver.getVariable("greeting").getValue())
        .isInstanceOf(TestRunnerException.class)
        .hasMessage("The variable 'greeting' has thrown an exception")
        .hasRootCauseMessage("catch me");
  }

  @Test
  void getVariableOrDefaultShouldReturnDefaultValue() {
    var variableResolver = new RunnerVariableResolver(emptySet(), emptySet(), variableDescriptor);

    var variable = variableResolver.getVariableOrDefault("greeting", "welcome");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome");
  }

  @Test
  void getVariableOrDefaultShouldReturnOverriddenValue() {
    var variableResolver = new RunnerVariableResolver(
        emptySet(),
        Set.of(new TestModifier("greeting", Map.of(), c -> "hello")),
        variableDescriptor);

    var variable = variableResolver.getVariableOrDefault("greeting", "welcome");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", MODIFIED)
        .hasFieldOrPropertyWithValue("value", "hello");
  }

  @Test
  void getVariableOrDefaultShouldReturnModifiedValue() {
    var variableResolver = new RunnerVariableResolver(
        emptySet(),
        Set.of(new TestModifier("greeting", Map.of(), c -> c.get("greeting") + " Bob")),
        variableDescriptor);

    var variable = variableResolver.getVariableOrDefault("greeting", "welcome");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", MODIFIED)
        .hasFieldOrPropertyWithValue("value", "welcome Bob");
  }

  @Test
  void getVariableOrDefaultShouldThrowExceptionWhenValueAlreadyDefined() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", Map.of(), c -> "hello")),
        emptySet(),
        variableDescriptor);

    Assertions
        .assertThatThrownBy(() -> variableResolver.getVariableOrDefault("greeting", "welcome"))
        .isInstanceOf(TestRunnerException.class)
        .hasMessage("The variable 'greeting' is already defined");
  }

  @Test
  void getVariableOrDefaultShouldThrowExceptionWhenException() {
    var variableResolver = new RunnerVariableResolver(
        emptySet(),
        Set.of(
            new TestModifier("greeting", Map.of(), c -> {
              throw new Exception("catch me");
            })),
        variableDescriptor);

    Assertions
        .assertThatThrownBy(() -> variableResolver.getVariableOrDefault("greeting", "hello").getValue())
        .isInstanceOf(TestRunnerException.class)
        .hasMessage("The variable 'greeting' has thrown an exception")
        .hasRootCauseMessage("catch me");
  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class RegisterListenerTest {
    private final RunnerVariableResolver variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("var-1", Map.of(), c -> "val-1"),
            new TestVariable("var-2", Map.of(), c -> "val-2"),
            new TestVariable("var-3", Map.of(), c -> "val-3")),
        Set.of(
            new TestModifier("var-2", Map.of(), c -> "ovr-2"),
            new TestModifier("var-3", Map.of(), c -> c.get("var-3") + " mod-3"),
            new TestModifier("var-5", Map.of(), c -> "ovr-5"),
            new TestModifier("var-6", Map.of(), c -> c.get("var-6") + " mod-6")),
        variableDescriptor);

    @Mock
    private RunnerVariableResolver.Listener listener;

    @Captor
    private ArgumentCaptor<Variable> variableCaptor;

    @BeforeEach
    void setUp() {
      variableResolver.register(listener);
    }

    @AfterEach
    void tearDown() {
      variableResolver.register(null);
    }

    @Test
    void getVariableShouldInvokedListenerWhenOriginalVariableIsLoaded() {
      variableResolver.getVariable("var-1").getValue();

      Mockito.verify(listener).variable(variableCaptor.capture());
      Mockito.verifyNoMoreInteractions(listener);
      assertThat(variableCaptor.getValue())
          .hasFieldOrPropertyWithValue("name", "var-1")
          .hasFieldOrPropertyWithValue("type", ORIGINAL)
          .hasFieldOrPropertyWithValue("value", "val-1");
    }

    @Test
    void getVariableShouldInvokedListenerWhenOverriddenVariableIsLoaded() {
      variableResolver.getVariable("var-2").getValue();

      Mockito.verify(listener).variable(variableCaptor.capture());
      Mockito.verifyNoMoreInteractions(listener);
      assertThat(variableCaptor.getValue())
          .hasFieldOrPropertyWithValue("name", "var-2")
          .hasFieldOrPropertyWithValue("type", MODIFIED)
          .hasFieldOrPropertyWithValue("value", "ovr-2");
    }

    @Test
    void getVariableShouldInvokedListenerWhenModifiedVariableIsLoaded() {
      variableResolver.getVariable("var-3").getValue();

      Mockito.verify(listener, Mockito.times(2)).variable(variableCaptor.capture());
      Mockito.verifyNoMoreInteractions(listener);
      var values = variableCaptor.getAllValues();
      assertThat(values)
          .element(0)
          .hasFieldOrPropertyWithValue("name", "var-3")
          .hasFieldOrPropertyWithValue("type", ORIGINAL)
          .hasFieldOrPropertyWithValue("value", "val-3");
      assertThat(values)
          .element(1)
          .hasFieldOrPropertyWithValue("name", "var-3")
          .hasFieldOrPropertyWithValue("type", MODIFIED)
          .hasFieldOrPropertyWithValue("value", "val-3 mod-3");
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenOriginalVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-4", "dft-4").getValue();

      Mockito.verify(listener).variable(variableCaptor.capture());
      Mockito.verifyNoMoreInteractions(listener);
      assertThat(variableCaptor.getValue())
          .hasFieldOrPropertyWithValue("name", "var-4")
          .hasFieldOrPropertyWithValue("type", ORIGINAL)
          .hasFieldOrPropertyWithValue("value", "dft-4");
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenOverriddenVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-5", "dft-5").getValue();

      Mockito.verify(listener).variable(variableCaptor.capture());
      Mockito.verifyNoMoreInteractions(listener);
      assertThat(variableCaptor.getValue())
          .hasFieldOrPropertyWithValue("name", "var-5")
          .hasFieldOrPropertyWithValue("type", MODIFIED)
          .hasFieldOrPropertyWithValue("value", "ovr-5");
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenModifiedVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-6", "dft-6").getValue();

      Mockito.verify(listener, Mockito.times(2)).variable(variableCaptor.capture());
      Mockito.verifyNoMoreInteractions(listener);
      var values = variableCaptor.getAllValues();
      assertThat(values)
          .element(0)
          .hasFieldOrPropertyWithValue("name", "var-6")
          .hasFieldOrPropertyWithValue("type", ORIGINAL)
          .hasFieldOrPropertyWithValue("value", "dft-6");
      assertThat(values)
          .element(1)
          .hasFieldOrPropertyWithValue("name", "var-6")
          .hasFieldOrPropertyWithValue("type", MODIFIED)
          .hasFieldOrPropertyWithValue("value", "dft-6 mod-6");
    }
  }
}
