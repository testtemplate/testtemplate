package io.github.testtemplate.core.builder;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.TestType;
import io.github.testtemplate.api.builder.AlternativeBuilder;
import io.github.testtemplate.api.builder.DefaultBuilder;
import io.github.testtemplate.api.builder.SetupBuilder;
import io.github.testtemplate.api.builder.SuiteBuilder;
import io.github.testtemplate.api.function.ExceptionalConsumer;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.suite.TestSuiteFactory;
import io.github.testtemplate.core.TestDefinition;
import io.github.testtemplate.core.TestModifier;
import io.github.testtemplate.core.TestParameter;
import io.github.testtemplate.core.TestTemplate;
import io.github.testtemplate.core.TestValidator;
import io.github.testtemplate.core.TestVariable;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;

public final class CoreBuilder {

  private static final String FRAMEWORK_PACKAGE_PREFIX = "io.github.testtemplate.";

  private CoreBuilder() {}

  public static <S> DefaultBuilder<S> builder(TestSuiteFactory<S> factory, TestInstantiator instantiator) {
    return new InnerDefaultBuilder<>(factory, instantiator);
  }

  static StackTraceElement captureFunctionSource() {
    StackTraceElement[] frames = Thread.currentThread().getStackTrace();
    StackTraceElement last = frames[frames.length - 1];
    for (int i = 1; i < frames.length; i++) {
      if (!frames[i].getClassName().startsWith(FRAMEWORK_PACKAGE_PREFIX)) {
        return frames[i];
      }
      last = frames[i];
    }
    return last;
  }

  private static final class InnerDefaultBuilder<S> implements DefaultBuilder<S> {

    private final TestSuiteFactory<S> factory;

    private final TestInstantiator instantiator;

    private InnerDefaultBuilder(TestSuiteFactory<S> factory, TestInstantiator instantiator) {
      this.factory = factory;
      this.instantiator = instantiator;
    }

    @Override
    public TestMetadataStep<S> defaultTest(String name) {
      return new InnerGivenAndWhenSteps(name);
    }

    private final class InnerGivenAndWhenSteps
        implements TestMetadataStep<S>, SetupStep<S>, GivenStep<S>, WhenStep<S> {

      private final String name;

      private final Map<String, @Nullable Object> metadata = new HashMap<>();

      private final Map<String, TestVariable> variables = new LinkedHashMap<>();

      private InnerGivenAndWhenSteps(String name) {
        this.name = name;
      }

      @Override
      public TestMetadataStep<S> metadata(String key, @Nullable Object value) {
        metadata.put(key, value);
        return this;
      }

      @Override
      public GivenStep<S> setUp(ExceptionalConsumer<SetupBuilder> setup) {
        try {
          setup.accept(new InnerSetupBuilder(metadata, variables));
          return this;
        } catch (Throwable e) {
          throw new TestBuilderException("Caught exception");
        }
      }

      @Override
      public ExtensionStep<S> given(String variable) {
        if (variables.containsKey(variable)) {
          throw new TestBuilderException("The variable '" + variable + "' is already defined");
        }
        return new InnerValueStep(variable);
      }

      @Override
      public <R> ThenStep<S, R> when(ExceptionalFunction<ContextGiven, R> template) {
        return new InnerThenStep<>(name, metadata, variables, new TestTemplate<>(template, captureFunctionSource()));
      }

      private final class InnerValueStep implements MetadataStep<S>, ExtensionStep<S>, ValueStep<S> {

        private final String variable;

        private final Map<String, @Nullable Object> metadata = new HashMap<>();

        private InnerValueStep(String variable) {
          this.variable = variable;
        }

        @Override
        public MetadataStep<S> metadata(String key, @Nullable Object value) {
          metadata.put(key, value);
          return this;
        }

        @Override
        public <M extends Extension<S>> M as(ExtensionFactory<S, M> factory) {
          return factory.getExtension(this, variable);
        }

        @Override
        public GivenStep<S> is(ExceptionalFunction<ContextGiven, ?> value) {
          variables.put(variable, new TestVariable(variable, metadata, value));
          return InnerGivenAndWhenSteps.this;
        }
      }
    }

    private final class InnerThenStep<R> implements ThenStep<S, R> {

      private final String name;

      private final Map<String, @Nullable Object> metadata;

      private final Map<String, TestVariable> variables;

      private final TestTemplate<R> template;

      private InnerThenStep(
          String name,
          Map<String, @Nullable Object> metadata,
          Map<String, TestVariable> variables,
          TestTemplate<R> template) {
        this.name = name;
        this.variables = variables;
        this.metadata = metadata;
        this.template = template;
      }

      @Override
      public AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator) {
        var test = new TestDefinition<>(
            TestType.DEFAULT,
            name,
            metadata,
            variables.values(),
            Collections.emptyList(),
            Collections.emptyList(),
            template,
            new TestValidator<>(validator, captureFunctionSource()));
        return new InnerAlternativeBuilder<>(factory, instantiator, variables, template, test);
      }
    }
  }

  private static final class InnerAlternativeBuilder<S, R> implements AlternativeBuilder<S, R>, SuiteBuilder<S> {

    private final TestSuiteFactory<S> factory;

    private final TestInstantiator instantiator;

    private final Map<String, TestVariable> variables;

    private final TestTemplate<R> template;

    private final List<TestDefinition<R>> tests = new ArrayList<>();

    private InnerAlternativeBuilder(
        TestSuiteFactory<S> factory,
        TestInstantiator instantiator,
        Map<String, TestVariable> variables,
        TestTemplate<R> template,
        TestDefinition<R> defaultTest) {
      this.factory = factory;
      this.instantiator = instantiator;
      this.variables = Map.copyOf(variables);
      this.template = template;
      this.tests.add(defaultTest);
    }

    @Override
    public TestMetadataStep<S, R> test(String name) {
      return new ExceptAndThenStep(name);
    }

    @Override
    public S suite() {
      var instances = tests.stream().map(instantiator::instantiate);
      return factory.getSuite(instances);
    }

    private final class ExceptAndThenStep
        implements TestMetadataStep<S, R>, SameStep<S, R>, ExceptStep<S, R>, ThenStep<S, R> {

      private final String name;

      private final Map<String, @Nullable Object> metadata = new HashMap<>();

      private final Map<String, TestModifier> modifiers = new LinkedHashMap<>();

      private final Map<String, TestParameter> parameters = new LinkedHashMap<>();

      private ExceptAndThenStep(String name) {
        this.name = name;
      }

      @Override
      public TestMetadataStep<S, R> metadata(String key, @Nullable Object value) {
        metadata.put(key, value);
        return this;
      }

      @Override
      public ExceptStep<S, R> sameAsDefault() {
        return this;
      }

      @Override
      public ExtensionStep<S, R> except(String variable) {
        return new InnerValueStep(variable);
      }

      @Override
      public Value2Step<S, R> except(
          String variable1,
          String variable2) {
        return new InnerValueNStep(List.of(variable1, variable2));
      }

      @Override
      public Value3Step<S, R> except(
          String variable1,
          String variable2,
          String variable3) {
        return new InnerValueNStep(List.of(variable1, variable2, variable3));
      }

      @Override
      public Value4Step<S, R> except(
          String variable1,
          String variable2,
          String variable3,
          String variable4) {
        return new InnerValueNStep(List.of(variable1, variable2, variable3, variable4));
      }

      @Override
      public Value5Step<S, R> except(
          String variable1,
          String variable2,
          String variable3,
          String variable4,
          String variable5) {
        return new InnerValueNStep(List.of(variable1, variable2, variable3, variable4, variable5));
      }

      @Override
      public ValueNStep<S, R> except(List<String> variables) {
        return new InnerValueNStep(variables);
      }

      @Override
      public AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator) {
        var test = new TestDefinition<>(
            TestType.ALTERNATIVE,
            name,
            metadata,
            variables.values(),
            modifiers.values(),
            parameters.values(),
            template,
            new TestValidator<>(validator, captureFunctionSource()));
        tests.add(test);
        return InnerAlternativeBuilder.this;
      }

      private final class InnerValueStep
          implements MetadataStep<S, R>, ExtensionStep<S, R>, ValueStep<S, R> {

        private final String variable;

        private final Map<String, @Nullable Object> varMetadata = new HashMap<>();

        private InnerValueStep(String variable) {
          this.variable = variable;
        }

        @Override
        public MetadataStep<S, R> metadata(String key, @Nullable Object value) {
          varMetadata.put(key, value);
          return this;
        }

        @Override
        public <M extends Extension<S, R>> M as(ExtensionFactory<S, R, M> factory) {
          return factory.getExtension(this, variable);
        }

        @Override
        public PostStep<S, R> is(ExceptionalFunction<Context, ?> value) {
          return new InnerPostStep(variable, varMetadata, value);
        }
      }

      private final class InnerPostStep implements PostStep<S, R> {

        private final String variable;

        private final Map<String, @Nullable Object> varMetadata;

        private final List<ExceptionalFunction<Context, ?>> values = new ArrayList<>();

        private InnerPostStep(
            String variable,
            Map<String, @Nullable Object> varMetadata,
            ExceptionalFunction<Context, ?> value) {
          this.variable = variable;
          this.varMetadata = varMetadata;
          this.values.add(value);
        }

        @Override
        public PostStep<S, R> or(ExceptionalFunction<Context, ?> value) {
          values.add(value);
          return this;
        }

        @Override
        public ExtensionStep<S, R> except(String variable) {
          addThisAsModifierOrParameter();
          return ExceptAndThenStep.this.except(variable);
        }

        @Override
        public Value2Step<S, R> except(
            String variable1,
            String variable2) {
          addThisAsModifierOrParameter();
          return ExceptAndThenStep.this.except(variable1, variable2);
        }

        @Override
        public Value3Step<S, R> except(
            String variable1,
            String variable2,
            String variable3) {
          addThisAsModifierOrParameter();
          return ExceptAndThenStep.this.except(variable1, variable2, variable3);
        }

        @Override
        public Value4Step<S, R> except(
            String variable1,
            String variable2,
            String variable3,
            String variable4) {
          addThisAsModifierOrParameter();
          return ExceptAndThenStep.this.except(variable1, variable2, variable3, variable4);
        }

        @Override
        public Value5Step<S, R> except(
            String variable1,
            String variable2,
            String variable3,
            String variable4,
            String variable5) {
          addThisAsModifierOrParameter();
          return ExceptAndThenStep.this.except(variable1, variable2, variable3, variable4, variable5);
        }

        @Override
        public ValueNStep<S, R> except(List<String> variables) {
          addThisAsModifierOrParameter();
          return ExceptAndThenStep.this.except(variables);
        }

        @Override
        public AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator) {
          addThisAsModifierOrParameter();
          return ExceptAndThenStep.this.then(validator);
        }

        private void addThisAsModifierOrParameter() {
          if (values.size() == 1) {
            modifiers.put(variable, new TestModifier(variable, varMetadata, values.getFirst()));
          } else {
            parameters.put(variable, new TestParameter(variable, variable, varMetadata, values));
          }
        }
      }

      private final class InnerValueNStep
          implements ValueNStep<S, R>, Value2Step<S, R>, Value3Step<S, R>, Value4Step<S, R>, Value5Step<S, R> {

        private final List<String> variables;

        private InnerValueNStep(List<String> variables) {
          this.variables = variables;
        }

        @Override
        public InnerPostNStep are(List<ExceptionalFunction<Context, ?>> values) {
          if (values.size() != variables.size()) {
            throw new TestBuilderException("Expecting " + variables.size() + " values");
          }
          return new InnerPostNStep(variables, values);
        }

        @Override
        public Post2Step<S, R> are(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2) {
          return are(List.of(value1, value2));
        }

        @Override
        public Post3Step<S, R> are(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2,
            ExceptionalFunction<Context, ?> value3) {
          return are(List.of(value1, value2, value3));
        }

        @Override
        public Post4Step<S, R> are(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2,
            ExceptionalFunction<Context, ?> value3,
            ExceptionalFunction<Context, ?> value4) {
          return are(List.of(value1, value2, value3, value4));
        }

        @Override
        public Post5Step<S, R> are(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2,
            ExceptionalFunction<Context, ?> value3,
            ExceptionalFunction<Context, ?> value4,
            ExceptionalFunction<Context, ?> value5) {
          return are(List.of(value1, value2, value3, value4, value5));
        }
      }

      private final class InnerPostNStep
          implements PostNStep<S, R>, Post2Step<S, R>, Post3Step<S, R>, Post4Step<S, R>, Post5Step<S, R> {

        private final List<String> variables;

        private final List<List<ExceptionalFunction<Context, ?>>> values = new ArrayList<>();

        private InnerPostNStep(List<String> variables, List<ExceptionalFunction<Context, ?>> values) {
          this.variables = variables;
          this.values.add(values);
        }

        @Override
        public InnerPostNStep or(List<ExceptionalFunction<Context, ?>> values) {
          if (values.size() != variables.size()) {
            throw new TestBuilderException("Expecting " + variables.size() + " values");
          }
          this.values.add(values);
          return this;
        }

        @Override
        public Post2Step<S, R> or(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2) {
          return or(List.of(value1, value2));
        }

        @Override
        public Post3Step<S, R> or(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2,
            ExceptionalFunction<Context, ?> value3) {
          return or(List.of(value1, value2, value3));
        }

        @Override
        public Post4Step<S, R> or(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2,
            ExceptionalFunction<Context, ?> value3,
            ExceptionalFunction<Context, ?> value4) {
          return or(List.of(value1, value2, value3, value4));
        }

        @Override
        public Post5Step<S, R> or(
            ExceptionalFunction<Context, ?> value1,
            ExceptionalFunction<Context, ?> value2,
            ExceptionalFunction<Context, ?> value3,
            ExceptionalFunction<Context, ?> value4,
            ExceptionalFunction<Context, ?> value5) {
          return or(List.of(value1, value2, value3, value4, value5));
        }

        @Override
        public ExtensionStep<S, R> except(String variable) {
          addThisAsModifiersOrParameters();
          return ExceptAndThenStep.this.except(variable);
        }

        @Override
        public Value2Step<S, R> except(
            String variable1,
            String variable2) {
          addThisAsModifiersOrParameters();
          return ExceptAndThenStep.this.except(variable1, variable2);
        }

        @Override
        public Value3Step<S, R> except(
            String variable1,
            String variable2,
            String variable3) {
          addThisAsModifiersOrParameters();
          return ExceptAndThenStep.this.except(variable1, variable2, variable3);
        }

        @Override
        public Value4Step<S, R> except(
            String variable1,
            String variable2,
            String variable3,
            String variable4) {
          addThisAsModifiersOrParameters();
          return ExceptAndThenStep.this.except(variable1, variable2, variable3, variable4);
        }

        @Override
        public Value5Step<S, R> except(
            String variable1,
            String variable2,
            String variable3,
            String variable4,
            String variable5) {
          addThisAsModifiersOrParameters();
          return ExceptAndThenStep.this.except(variable1, variable2, variable3, variable4, variable5);
        }

        @Override
        public ValueNStep<S, R> except(List<String> variables) {
          addThisAsModifiersOrParameters();
          return ExceptAndThenStep.this.except(variables);
        }

        @Override
        public AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator) {
          addThisAsModifiersOrParameters();
          return ExceptAndThenStep.this.then(validator);
        }

        private void addThisAsModifiersOrParameters() {
          if (values.size() == 1) {
            for (int i = 0; i < variables.size(); i++) {
              metadata.put("io.github.testtemplate.variable.order", i);
              modifiers.put(variables.get(i), new TestModifier(variables.get(i), metadata, values.getFirst().get(i)));
            }
          } else {
            var group = join("|", variables);
            var pivotValues = pivotValues();
            for (int i = 0; i < variables.size(); i++) {
              metadata.put("io.github.testtemplate.variable.order", i);
              parameters.put(
                  variables.get(i),
                  new TestParameter(variables.get(i), group, metadata, pivotValues.get(i)));
            }
          }
        }

        private List<List<ExceptionalFunction<Context, ?>>> pivotValues() {
          var pivot = new ArrayList<List<ExceptionalFunction<Context, ?>>>();

          for (int i = 0; i < variables.size(); i++) {
            pivot.add(new ArrayList<>());
          }

          for (var value : values) {
            for (int j = 0; j < variables.size(); j++) {
              pivot.get(j).add(value.get(j));
            }
          }

          return pivot;
        }
      }
    }
  }

  private static final class InnerSetupBuilder implements SetupBuilder {

    private final Map<String, @Nullable Object> metadata;

    private final Map<String, TestVariable> variables;

    private InnerSetupBuilder(Map<String, @Nullable Object> metadata, Map<String, TestVariable> variables) {
      this.metadata = metadata;
      this.variables = variables;
    }

    @Override
    public SetupBuilder metadata(String key, @Nullable Object value) {
      metadata.put(key, value);
      return this;
    }

    @Override
    public ExtensionStep given(String variable) {
      return new InnerValueStep(variable);
    }

    private final class InnerGivenStep implements GivenStep {

      @Override
      public ExtensionStep given(String variable) {
        return new InnerValueStep(variable);
      }
    }

    private final class InnerValueStep implements ExtensionStep, MetadataStep, ValueStep {

      private final String variable;

      private final Map<String, @Nullable Object> metadata = new HashMap<>();

      private InnerValueStep(String variable) {
        this.variable = variable;
      }

      @Override
      public <M extends Extension> M as(ExtensionFactory<M> factory) {
        return factory.getExtension(this, variable);
      }

      @Override
      public MetadataStep metadata(String key, @Nullable Object value) {
        metadata.put(key, value);
        return this;
      }

      @Override
      public GivenStep is(ExceptionalFunction<ContextGiven, ?> value) {
        variables.put(variable, new TestVariable(variable, metadata, value));
        return new InnerGivenStep();
      }
    }
  }
}
