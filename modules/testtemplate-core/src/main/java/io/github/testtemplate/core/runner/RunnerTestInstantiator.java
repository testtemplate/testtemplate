package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.TestType;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.VariableType;
import io.github.testtemplate.api.config.Configuration;
import io.github.testtemplate.api.function.ExceptionalConsumer;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.suite.TestSuiteFactory;
import io.github.testtemplate.core.TestDefinition;
import io.github.testtemplate.core.TestModifier;
import io.github.testtemplate.core.TestParameter;
import io.github.testtemplate.core.TestVariable;
import io.github.testtemplate.core.builder.TestInstantiator;
import io.github.testtemplate.core.runner.listener.CustomTestListener;
import io.github.testtemplate.core.runner.listener.DisabledTestListener;
import io.github.testtemplate.core.runner.listener.LoggerListener;
import io.github.testtemplate.core.runner.listener.PreloadVariableListener;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class RunnerTestInstantiator implements TestInstantiator {

  static final String RUNNER_METADATA_LEVEL = "io.github.testtemplate.runner.level";
  static final String RUNNER_METADATA_INDEXES = "io.github.testtemplate.runner.indexes";
  static final String RUNNER_METADATA_TEMPLATE_SOURCE = "io.github.testtemplate.runner.templateSource";

  static final String VARIABLE_METADATA_LEVEL = "io.github.testtemplate.variable.level";

  private final Configuration configuration;

  private final RunnerVariableDescriptor variableDescriptor = new RunnerVariableDescriptor();

  private final AtomicInteger firstLevelIndex = new AtomicInteger(0);

  public RunnerTestInstantiator(final Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public <R> TestSuiteFactory.Test instantiate(TestDefinition<R> definition) {
    final var metadata = new HashMap<>(definition.getMetadata());
    metadata.put(RUNNER_METADATA_LEVEL, 0);
    metadata.put(RUNNER_METADATA_INDEXES, Indexes.index(firstLevelIndex.incrementAndGet()));
    metadata.put(RUNNER_METADATA_TEMPLATE_SOURCE, definition.getTemplate().getSource());

    return instantiate(
        definition.getType(),
        definition.getName(),
        metadata,
        new ArrayList<>(definition.getVariables()),
        new ArrayList<>(definition.getModifiers()),
        new ArrayList<>(definition.getParameters()),
        definition.getTemplate().getFunction(),
        definition.getValidator().getFunction());
  }

  private <R> TestSuiteFactory.Test instantiate(
      TestType type,
      String name,
      Map<String, @Nullable Object> metadata,
      List<TestVariable> variables,
      List<TestModifier> modifiers,
      List<TestParameter> parameters,
      ExceptionalFunction<ContextGiven, R> template,
      ExceptionalConsumer<ContextResult<R>> validator) {

    if (parameters.isEmpty()) {
      return instantiateItem(type, name, metadata, variables, modifiers, template, validator);
    } else {
      return instantiateGroup(type, name, metadata, variables, modifiers, parameters, template, validator);
    }
  }

  private <R> TestSuiteFactory.Test instantiateItem(
      TestType type,
      String name,
      Map<String, @Nullable Object> metadata,
      List<TestVariable> variables,
      List<TestModifier> modifiers,
      ExceptionalFunction<ContextGiven, R> template,
      ExceptionalConsumer<ContextResult<R>> validator) {

    var variableResolver = new RunnerVariableResolver(variables, modifiers, variableDescriptor);
    var nameSubstitutor = new TestItemNameSubstitutor(name, variableResolver, metadata);
    var resolvedName = nameSubstitutor.getTestItemName();
    var testRunner = new TestRunner<>(template, validator, variableResolver);
    var listeners = new ListenerAdapter<R>(
        List.of(
            new DisabledTestListener(),
            new LoggerListener(),
            new CustomTestListener(),
            new PreloadVariableListener()),
        new RunnerTest(resolvedName, type, variableResolver, metadata));
    variableResolver.register(listeners);
    testRunner.register(listeners);

    return new TestItemInstance<>(resolvedName, testRunner);
  }

  private <R> TestSuiteFactory.Test instantiateGroup(
      TestType type,
      String name,
      Map<String, @Nullable Object> metadata,
      List<TestVariable> variables,
      List<TestModifier> modifiers,
      List<TestParameter> parameters,
      ExceptionalFunction<ContextGiven, R> template,
      ExceptionalConsumer<ContextResult<R>> validator) {

    var variableResolver = new RunnerVariableResolver(variables, modifiers, variableDescriptor);
    variableResolver.register(new GroupSharedValueEnforcer(variables, modifiers));

    var nameSubstitutor = new TestGroupNameSubstitutor(name, variableResolver, metadata);
    var resolvedName = nameSubstitutor.getTestGroupName();

    TestParameter firstParameter = parameters.getFirst();

    var tests = IntStream
        .range(0, firstParameter.getValueSuppliers().size())
        .mapToObj(index -> {
          int newLevel = (int) Objects.requireNonNull(metadata.get(RUNNER_METADATA_LEVEL)) + 1;

          List<TestModifier> newModifiers = new ArrayList<>(modifiers);
          List<TestParameter> newParameters = new ArrayList<>(parameters);

          for (TestParameter parameter : getParametersOf(parameters, firstParameter.getGroup())) {
            newModifiers.add(toModifier(parameter, index, newLevel));
            newParameters.remove(parameter);
          }

          Indexes indexes = (Indexes) Objects.requireNonNull(metadata.get(RUNNER_METADATA_INDEXES));
          var newMetadata = new HashMap<>(metadata);
          newMetadata.put(RUNNER_METADATA_LEVEL, newLevel);
          newMetadata.put(RUNNER_METADATA_INDEXES, indexes.subIndex(index));

          return instantiate(
              type,
              nameSubstitutor.getTestItemName(),
              newMetadata,
              variables,
              newModifiers,
              newParameters,
              template,
              validator);
        });

    return new TestGroupInstance(resolvedName, tests);
  }

  private static List<TestParameter> getParametersOf(List<TestParameter> parameters, String group) {
    return parameters.stream().filter(p -> p.getGroup().equals(group)).toList();
  }

  private static TestModifier toModifier(TestParameter parameter, int index, int level) {
    var newMetadata = new HashMap<>(parameter.getMetadata());
    newMetadata.put(VARIABLE_METADATA_LEVEL, level);

    return new TestModifier(
        parameter.getName(),
        newMetadata,
        parameter.getValueSuppliers().get(index));
  }

  public interface Listener {

    void before(Test test);

    void after(Test test);

    void result(Test test, @Nullable Object result);

    void exception(Test test, Throwable exception);

    void variable(Test test, Variable variable);

  }

  private static final class ListenerAdapter<R> implements TestRunner.Listener<R>, RunnerVariableResolver.Listener {

    private final List<Listener> listeners;

    private final Test test;

    private ListenerAdapter(List<Listener> listeners, Test test) {
      this.listeners = listeners;
      this.test = test;
    }

    @Override
    public void before() {
      listeners.forEach(l -> l.before(test));
    }

    @Override
    public void after() {
      listeners.forEach(l -> l.after(test));
    }

    @Override
    public void result(@Nullable R result) {
      listeners.forEach(l -> l.result(test, result));
    }

    @Override
    public void exception(Throwable exception) {
      listeners.forEach(l -> l.exception(test, exception));
    }

    @Override
    public void variable(Variable variable) {
      listeners.forEach(l -> l.variable(test, variable));
    }
  }

  private static final class TestItemInstance<R> implements TestSuiteFactory.TestItem {

    private final String name;
    private final TestRunner<R> runner;

    private TestItemInstance(String name, TestRunner<R> runner) {
      this.runner = runner;
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void execute() {
      runner.execute();
    }
  }

  private static final class TestGroupInstance implements TestSuiteFactory.TestGroup {

    private final String name;

    private final Stream<? extends TestSuiteFactory.Test> tests;

    private TestGroupInstance(String name, Stream<? extends TestSuiteFactory.Test> tests) {
      this.name = name;
      this.tests = tests;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Stream<? extends TestSuiteFactory.Test> getTests() {
      return tests;
    }
  }

  private static final class GroupSharedValueEnforcer implements RunnerVariableResolver.Listener {

    private final List<TestVariable> variables;
    private final List<TestModifier> modifiers;

    private GroupSharedValueEnforcer(List<TestVariable> variables, List<TestModifier> modifiers) {
      this.variables = variables;
      this.modifiers = modifiers;
    }

    @Override
    public void variable(Variable variable) {
      if (variable.getType() == VariableType.ORIGINAL) {
        variables.replaceAll(v -> v.getName().equals(variable.getName())
            ? new TestVariable(v.getName(), v.getMetadata(), ctx -> variable.getValue())
            : v);
      } else {
        modifiers.replaceAll(m -> m.getName().equals(variable.getName())
            ? new TestModifier(m.getName(), m.getMetadata(), ctx -> variable.getValue())
            : m);
      }
    }
  }
  
//  private static final class CachedExceptionalFunction<T, R> implements ExceptionalFunction<T, R> {
//
//    private final ExceptionalFunction<T, R> delegate;
//
//    private boolean loaded;
//
//    @Nullable
//    private R value;
//
//    @Nullable
//    private Exception exception;
//
//    private CachedExceptionalFunction(ExceptionalFunction<T, R> delegate) {
//      this.delegate = delegate;
//    }
//
//    @Override
//    public R apply(T t) throws Exception {
//      if (!loaded) {
//        try {
//          value = delegate.apply(t);
//          loaded = true;
//        } catch (Exception e) {
//          exception = e;
//          loaded = true;
//        }
//      }
//
//      if (exception != null) {
//        throw exception;
//      }
//
//      return value;
//    }
//  }
}
