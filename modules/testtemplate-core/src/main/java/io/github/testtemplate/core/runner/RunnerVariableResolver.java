package io.github.testtemplate.core.runner;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.VariableType;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.core.TestModifier;
import io.github.testtemplate.core.TestVariable;

final class RunnerVariableResolver {

  private final Map<String, InnerVariable> variables = new HashMap<>();

  private final Map<String, InnerModifier> modifiers = new HashMap<>();

  private final RunnerVariableDescriptor descriptor;

  private Listener listener = new NoOpListener();

  RunnerVariableResolver(
      Iterable<TestVariable> variables,
      Iterable<TestModifier> modifiers,
      RunnerVariableDescriptor descriptor) {
    variables.forEach(
        variable -> this.variables.put(
            variable.getName(),
            new InnerVariable(variable.getValueSupplier(), variable.getMetadata())));

    modifiers.forEach(
        modifier -> this.modifiers.put(
            modifier.getName(),
            new InnerModifier(modifier.getValueSupplier(), modifier.getMetadata())));

    this.descriptor = descriptor;
  }

  private RunnerVariableResolver(
      Map<String, InnerVariable> variables,
      Map<String, InnerModifier> modifiers,
      RunnerVariableDescriptor descriptor,
      Listener listener) {
    this.variables.putAll(variables);
    this.modifiers.putAll(modifiers);
    this.descriptor = descriptor;
    this.listener = listener;
  }

  public void register(@Nullable Listener listener) {
    this.listener = listener != null ? listener : new NoOpListener();
  }

  public Iterable<String> getVariableNames() {
    return Stream
        .concat(variables.keySet().stream(), modifiers.keySet().stream())
        .collect(Collectors.toUnmodifiableSet());
//    return unmodifiableSet(variables.keySet());
  }

  public Variable getVariable(String name) {
    var modifier = this.modifiers.get(name);
    if (modifier != null) {
      var newResolver = this.copy().withoutModifier(name);
      var newContext = new RunnerContext(newResolver);
      return new RunnerVariable(
          name,
          VariableType.MODIFIED,
          () -> {
            var value = modifier.getValueSupplier().apply(newContext);
            listener.variable(
                new RunnerVariable(name, VariableType.MODIFIED, () -> value, descriptor, modifier.getMetadata()));
            return value;
          },
          descriptor,
          modifier.getMetadata());
    }

    var variable = this.variables.get(name);
    if (variable != null) {
      var newResolver = this.copy().withoutVariable(name);
      var newContext = new RunnerContextGiven(newResolver);
      return new RunnerVariable(
          name,
          VariableType.ORIGINAL,
          () -> {
            var value = variable.getValueSupplier().apply(newContext);
            listener.variable(
                new RunnerVariable(name, VariableType.ORIGINAL, () -> value, descriptor, variable.getMetadata()));
            return value;
          },
          descriptor,
          variable.getMetadata());
    }

    throw new TestRunnerException("The variable '" + name + "' is undefined");
  }

  public Variable getVariableOrDefault(String name, Object defaultValue) {
    var modifier = this.modifiers.get(name);
    if (modifier != null) {
      var newResolver = this.copy()
          .withVariable(name, new InnerVariable(c -> defaultValue))
          .withoutModifier(name);
      var newContext = new RunnerContext(newResolver);
      return new RunnerVariable(
          name,
          VariableType.MODIFIED,
          () -> {
            var value = modifier.getValueSupplier().apply(newContext);
            listener.variable(
                new RunnerVariable(name, VariableType.MODIFIED, () -> value, descriptor, modifier.getMetadata()));
            return value;
          },
          descriptor,
          modifier.getMetadata());
    }

    var variable = variables.get(name);
    if (variable != null) {
      throw new TestRunnerException("The variable '" + name + "' is already defined");
    }

    return new RunnerVariable(
        name,
        VariableType.ORIGINAL,
        () -> {
          listener.variable(new RunnerVariable(name, VariableType.ORIGINAL, () -> defaultValue, descriptor));
          return defaultValue;
        },
        descriptor);
  }

  private RunnerVariableResolver copy() {
    return new RunnerVariableResolver(variables, modifiers, descriptor, listener);
  }

  private RunnerVariableResolver withVariable(String name, InnerVariable variable) {
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

  interface Listener {

    void variable(Variable variable);

  }

  private static final class NoOpListener implements Listener {

    @Override
    public void variable(Variable variable) {
    }
  }

  private static final class InnerVariable {

    private final ExceptionalFunction<ContextGiven, ?> valueSupplier;

    private final Map<String, Object> metadata = new HashMap<>();

    private InnerVariable(ExceptionalFunction<ContextGiven, ?> valueSupplier) {
      this.valueSupplier = new CachedExceptionalFunction<>(valueSupplier);
    }

    private InnerVariable(ExceptionalFunction<ContextGiven, ?> valueSupplier, Map<String, Object> metadata) {
      this.valueSupplier = new CachedExceptionalFunction<>(valueSupplier);
      this.metadata.putAll(metadata);
    }

    public ExceptionalFunction<ContextGiven, ?> getValueSupplier() {
      return valueSupplier;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }
  }

  private static final class InnerModifier {

    private final ExceptionalFunction<Context, ?> valueSupplier;

    private final Map<String, Object> metadata = new HashMap<>();

    private InnerModifier(ExceptionalFunction<Context, ?> valueSupplier, Map<String, Object> metadata) {
      this.valueSupplier = new CachedExceptionalFunction<>(valueSupplier);
      this.metadata.putAll(metadata);
    }

    public ExceptionalFunction<Context, ?> getValueSupplier() {
      return valueSupplier;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }
  }

  private static final class CachedExceptionalFunction<T, R> implements ExceptionalFunction<T, R> {

    private final ExceptionalFunction<T, R> delegate;

    private boolean loaded;

    @Nullable
    private R value;

    @Nullable
    private Exception exception;

    private CachedExceptionalFunction(ExceptionalFunction<T, R> delegate) {
      this.delegate = delegate;
    }

    @Override
    public R apply(T t) throws Exception {
      if (!loaded) {
        try {
          value = delegate.apply(t);
          loaded = true;
        } catch (Exception e) {
          exception = e;
          loaded = true;
        }
      }

      if (exception != null) {
        throw exception;
      }

      return value;
    }
  }
}
