package com.github.testtemplate.core.runner;

import com.github.testtemplate.core.TestModifier;
import com.github.testtemplate.core.TestVariable;
import com.github.testtemplate.core.runner.RunnerVariableResolver.Listener;

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

import static com.github.testtemplate.TestListener.VariableType.MODIFIED;
import static com.github.testtemplate.TestListener.VariableType.ORIGINAL;
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
      Mockito.verify(listener).accept(Mockito.eq("var-1"), Mockito.eq(ORIGINAL), Mockito.eq("val-1"));
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableShouldInvokedListenerWhenOverriddenVariableIsLoaded() {
      variableResolver.getVariable("var-2").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-2"), Mockito.eq(MODIFIED), Mockito.eq("ovr-2"));
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableShouldInvokedListenerWhenModifiedVariableIsLoaded() {
      variableResolver.getVariable("var-3").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-3"), Mockito.eq(ORIGINAL), Mockito.eq("val-3"));
      Mockito.verify(listener).accept(Mockito.eq("var-3"), Mockito.eq(MODIFIED), Mockito.eq("val-3 mod-3"));
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenOriginalVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-4", "dft-4").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-4"), Mockito.eq(ORIGINAL), Mockito.eq("dft-4"));
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenOverriddenVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-5", "dft-5").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-5"), Mockito.eq(MODIFIED), Mockito.eq("ovr-5"));
      Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void getVariableOrDefaultShouldInvokedListenerWhenModifiedVariableIsLoaded() {
      variableResolver.getVariableOrDefault("var-6", "dft-6").getValue();
      Mockito.verify(listener).accept(Mockito.eq("var-6"), Mockito.eq(ORIGINAL), Mockito.eq("dft-6"));
      Mockito.verify(listener).accept(Mockito.eq("var-6"), Mockito.eq(MODIFIED), Mockito.eq("dft-6 mod-6"));
      Mockito.verifyNoMoreInteractions(listener);
    }
  }
}
