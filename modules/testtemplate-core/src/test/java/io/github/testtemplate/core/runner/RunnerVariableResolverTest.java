package io.github.testtemplate.core.runner;

import io.github.testtemplate.core.TestModifier;
import io.github.testtemplate.core.TestVariable;
import io.github.testtemplate.core.runner.RunnerVariableResolver.Listener;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.testtemplate.VariableType.MODIFIED;
import static io.github.testtemplate.VariableType.ORIGINAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

class RunnerVariableResolverTest {

  @Test
  void getVariableNamesShouldReturnOnlyVariableNames() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", c -> "?"),
            new TestVariable("first-name", c -> "?"),
            new TestVariable("last-name", c -> "?")),
        Set.of(
            new TestModifier("greeting", c -> "?"),
            new TestModifier("another", c -> "?")));

    var names = variableResolver.getVariableNames();

    assertThat(names).containsExactlyInAnyOrder("greeting", "first-name", "last-name");
  }

  @Test
  void getVariableShouldThrowExceptionWhenVariableIsUndefined() {
    var variableResolver = new RunnerVariableResolver(emptySet(), emptySet());
    Assertions
        .assertThatThrownBy(() -> variableResolver.getVariable("greeting"))
        .isInstanceOf(TestRunnerException.class)
        .hasMessage("The variable 'greeting' is undefined");
  }

  @Test
  void getVariableShouldReturnOriginalValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", c -> "welcome")),
        emptySet());

    var variable = variableResolver.getVariable("greeting");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome");
  }

  @Test
  void getVariableShouldReturnOverriddenValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", c -> "welcome")),
        Set.of(new TestModifier("greeting", c -> "hello")));

    var variable = variableResolver.getVariable("greeting");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", MODIFIED)
        .hasFieldOrPropertyWithValue("value", "hello");
  }

  @Test
  void getVariableShouldReturnModifiedValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", c -> "welcome")),
        Set.of(new TestModifier("greeting", c -> c.get("greeting") + " bob")));

    var variable = variableResolver.getVariable("greeting");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", MODIFIED)
        .hasFieldOrPropertyWithValue("value", "welcome bob");
  }

  @Test
  void getVariableShouldReturnComposedValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", c -> "welcome"),
            new TestVariable("name", c -> "bob"),
            new TestVariable("message", c -> c.get("greeting") + " " + c.get("name"))),
        emptySet());

    var variable = variableResolver.getVariable("message");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "message")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome bob");
  }

  @Test
  void getVariableShouldReturnComposedValueWithOverriddenValue() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("greeting", c -> "welcome"),
            new TestVariable("name", c -> "bob"),
            new TestVariable("message", c -> c.get("greeting") + " " + c.get("name"))),
        Set.of(
            new TestModifier("name", c -> "alice")));

    var variable = variableResolver.getVariable("message");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "message")
        .hasFieldOrPropertyWithValue("type", ORIGINAL)
        .hasFieldOrPropertyWithValue("value", "welcome alice");
  }

  @Test
  void getVariableShouldReturnMetadata() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", c -> "welcome", Map.of("test-key", "test-value"))),
        Set.of(new TestModifier("name", c -> "alice", Map.of("other-key", "other-value"))));

    var var1 = variableResolver.getVariable("greeting");

    assertThat(var1.getMetadata("test-key")).isEqualTo("test-value");

    var var2 = variableResolver.getVariable("name");

    assertThat(var2.getMetadata("other-key")).isEqualTo("other-value");
  }

  @Test
  void getVariableOrDefaultShouldReturnDefaultValue() {
    var variableResolver = new RunnerVariableResolver(emptySet(), emptySet());

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
        Set.of(new TestModifier("greeting", c -> "hello")));

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
        Set.of(new TestModifier("greeting", c -> c.get("greeting") + " bob")));

    var variable = variableResolver.getVariableOrDefault("greeting", "welcome");

    assertThat(variable)
        .hasFieldOrPropertyWithValue("name", "greeting")
        .hasFieldOrPropertyWithValue("type", MODIFIED)
        .hasFieldOrPropertyWithValue("value", "welcome bob");
  }

  @Test
  void getVariableOrDefaultShouldThrowExceptionWhenValueAlreadyDefined() {
    var variableResolver = new RunnerVariableResolver(
        Set.of(new TestVariable("greeting", c -> "hello")),
        emptySet());

    Assertions
        .assertThatThrownBy(() -> variableResolver.getVariableOrDefault("greeting", "welcome"))
        .isInstanceOf(TestRunnerException.class)
        .hasMessage("The variable 'greeting' is already defined");
  }

  @Test
  void getVariableShouldCallSupplierOnlyOnce() {
    var number1 = new AtomicInteger(1000);
    var number2 = new AtomicInteger(2000);
    var number3 = new AtomicInteger(3000);

    var variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("number-1", c -> number1.getAndIncrement()),
            new TestVariable("number-2", c -> { throw new RuntimeException("not reachable"); }),
            new TestVariable("number-3", c -> number3.getAndIncrement())),
        Set.of(
            new TestModifier("number-2", c -> number2.getAndIncrement()),
            new TestModifier("number-3", c -> c.<Integer>get("number-3") + 1000)));

    // Make few calls
    variableResolver.getVariable("number-1").getValue();
    variableResolver.getVariable("number-1").getValue();
    variableResolver.getVariable("number-1").getValue();
    variableResolver.getVariable("number-2").getValue();
    variableResolver.getVariable("number-2").getValue();
    variableResolver.getVariable("number-2").getValue();
    variableResolver.getVariable("number-3").getValue();
    variableResolver.getVariable("number-3").getValue();
    variableResolver.getVariable("number-3").getValue();

    assertThat(variableResolver.getVariable("number-1").getValue()).isEqualTo(1000);
    assertThat(variableResolver.getVariable("number-2").getValue()).isEqualTo(2000);
    assertThat(variableResolver.getVariable("number-3").getValue()).isEqualTo(4000);
  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class RegisterListenerTest {
    private final RunnerVariableResolver variableResolver = new RunnerVariableResolver(
        Set.of(
            new TestVariable("var-1", c -> "val-1"),
            new TestVariable("var-2", c -> "val-2"),
            new TestVariable("var-3", c -> "val-3")),
        Set.of(
            new TestModifier("var-2", c -> "ovr-2"),
            new TestModifier("var-3", c -> c.get("var-3") + " mod-3"),
            new TestModifier("var-5", c -> "ovr-5"),
            new TestModifier("var-6", c -> c.get("var-6") + " mod-6")));

    @Mock
    private Listener listener;

    @BeforeEach
    void setUp() {
      variableResolver.registerListener(listener);
    }

    @AfterEach
    void tearDown() {
      variableResolver.registerListener(null);
    }

    @Test
    void getVariableShouldInvokedListenerWhenOriginalVariableIsLoaded() {
      variableResolver.getVariable("var-1").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-1"), Mockito.eq(ORIGINAL), Mockito.eq("val-1"), Mockito.anyMap());
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableShouldInvokedListenerWhenOverriddenVariableIsLoaded() {
      variableResolver.getVariable("var-2").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-2"), Mockito.eq(MODIFIED), Mockito.eq("ovr-2"), Mockito.anyMap());
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableShouldInvokedListenerWhenModifiedVariableIsLoaded() {
      variableResolver.getVariable("var-3").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-3"), Mockito.eq(ORIGINAL), Mockito.eq("val-3"), Mockito.anyMap());
      Mockito
          .verify(listener)
          .accept(Mockito.eq("var-3"), Mockito.eq(MODIFIED), Mockito.eq("val-3 mod-3"), Mockito.anyMap());
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenOriginalVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-4", "dft-4").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-4"), Mockito.eq(ORIGINAL), Mockito.eq("dft-4"), Mockito.anyMap());
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenOverriddenVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-5", "dft-5").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-5"), Mockito.eq(MODIFIED), Mockito.eq("ovr-5"), Mockito.anyMap());
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenModifiedVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-6", "dft-6").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-6"), Mockito.eq(ORIGINAL), Mockito.eq("dft-6"), Mockito.anyMap());
      Mockito
          .verify(listener)
          .accept(Mockito.eq("var-6"), Mockito.eq(MODIFIED), Mockito.eq("dft-6 mod-6"), Mockito.anyMap());
      Mockito.verifyNoMoreInteractions(listener);
    }
  }
}
