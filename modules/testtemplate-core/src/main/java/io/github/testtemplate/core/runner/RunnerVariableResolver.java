package io.github.testtemplate.core.runner;

import io.github.testtemplate.Context;
import io.github.testtemplate.ContextView;
import io.github.testtemplate.VariableType;
import io.github.testtemplate.core.TestModifier;
import io.github.testtemplate.core.TestVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.github.testtemplate.VariableType.MODIFIED;
import static io.github.testtemplate.VariableType.ORIGINAL;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;

final class RunnerVariableResolver {

  private final Map<String, InnerVariable> variables = new HashMap<>();

  private final Map<String, InnerModifier> modifiers = new HashMap<>();

  private Listener listener = (name, type, value, metadata) -> {};

  RunnerVariableResolver(Iterable<TestVariable> variables, Iterable<TestModifier> modifiers) {
    variables.forEach(variable -> this.variables.put(
        variable.getName(),
        new InnerVariable(variable.getValueSupplier(), variable.getMetadata())));

    modifiers.forEach(modifier -> this.modifiers.put(
        modifier.getName(),
        new InnerModifier(modifier.getValueSupplier(), modifier.getMetadata())));
  }

  private RunnerVariableResolver(
      Map<String, InnerVariable> variables,
      Map<String, InnerModifier> modifiers,
      Listener listener) {
    this.variables.putAll(variables);
    this.modifiers.putAll(modifiers);
    this.listener = listener;
  }

  void registerListener(Listener listener) {
    this.listener = listener != null ? listener : (name, type, value, metadata) -> {};
  }

  public Iterable<String> getVariableNames() {
    return unmodifiableSet(variables.keySet());
  }

  public RunnerVariable getVariable(String name) {
    var modifier = modifiers.get(name);
    if (modifier != null) {
      var newResolver = this.copy().withoutModifier(name);
      var newContext = new RunnerContextView(newResolver);
      return new RunnerVariable(
          name,
          MODIFIED,
          () -> {
            var value = modifier.valueSupplier.apply(newContext);
            listener.accept(name, MODIFIED, value, modifier.metadata);
            return value;
          },
          modifier.metadata);
    }

    var variable = variables.get(name);
    if (variable != null) {
      var newResolver = this.copy().withoutVariable(name);
      var newContext = new RunnerContext(newResolver);
      return new RunnerVariable(
          name,
          ORIGINAL,
          () -> {
            var value = variable.valueSupplier.apply(newContext);
            listener.accept(name, ORIGINAL, value, variable.metadata);
            return value;
          },
          variable.metadata);
    }

    throw new TestRunnerException("The variable '" + name + "' is undefined");
  }

  public RunnerVariable getVariableOrDefault(String name, Object defaultValue) {
    var modifier = modifiers.get(name);
    if (modifier != null) {
      var newResolver = this.copy()
          .withVariable(name, new InnerVariable(c -> defaultValue))
          .withoutModifier(name);
      var newContext = new RunnerContextView(newResolver);
      return new RunnerVariable(
          name,
          MODIFIED,
          () -> {
            var value = modifier.valueSupplier.apply(newContext);
            listener.accept(name, MODIFIED, value, modifier.metadata);
            return value;
          },
          modifier.metadata);
    }

    var variable = variables.get(name);
    if (variable != null) {
      throw new TestRunnerException("The variable '" + name + "' is already defined");
    }

    listener.accept(name, ORIGINAL, defaultValue, emptyMap());
    return new RunnerVariable(name, ORIGINAL, () -> defaultValue);
  }

  private RunnerVariableResolver copy() {
    return new RunnerVariableResolver(variables, modifiers, listener);
  }

  public RunnerVariableResolver withVariable(String name, InnerVariable variable) {
    variables.put(name, variable);
    return this;
  }

  private RunnerVariableResolver withoutVariable(String name) {
    variables.remove(name);
    return this;
  }

  private RunnerVariableResolver withoutModifier(String name) {
    modifiers.remove(name);
    return this;
  }

  @FunctionalInterface
  public interface Listener {

    void accept(String name, VariableType type, Object value, Map<String, Object> metadata);

  }

  static final class InnerVariable {

    private final Function<Context, ?> valueSupplier;

    private final Map<String, Object> metadata = new HashMap<>();

    InnerVariable(Function<Context, ?> valueSupplier) {
      this.valueSupplier = new CachedFunction<>(valueSupplier);
    }

    InnerVariable(Function<Context, ?> valueSupplier, Map<String, Object> metadata) {
      this.valueSupplier = new CachedFunction<>(valueSupplier);
      this.metadata.putAll(metadata);
    }
  }

  static final class InnerModifier {

    private final Function<ContextView, ?> valueSupplier;

    private final Map<String, Object> metadata = new HashMap<>();

    InnerModifier(Function<ContextView, ?> valueSupplier, Map<String, Object> metadata) {
      this.valueSupplier = new CachedFunction<>(valueSupplier);
      this.metadata.putAll(metadata);
    }
  }

  static final class CachedFunction<T, R> implements Function<T, R> {

    private final Function<T, R> delegate;

    private boolean valueLoaded;

    private R value;

    CachedFunction(Function<T, R> delegate) {
      this.delegate = delegate;
    }

    @Override
    public R apply(T t) {
      if (!valueLoaded) {
        value = delegate.apply(t);
        valueLoaded = true;
      }
      return value;
    }
  }
}
