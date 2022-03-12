package com.github.testtemplate.core.builder;

import com.github.testtemplate.AlternativeTestTemplateBuilder;
import com.github.testtemplate.AlternativeTestTemplateExceptBuilder;
import com.github.testtemplate.AlternativeTestTemplatePreBuilder;
import com.github.testtemplate.AlternativeTestValidatorBuilder;
import com.github.testtemplate.Context;
import com.github.testtemplate.ContextView;
import com.github.testtemplate.ContextualTemplate;
import com.github.testtemplate.ContextualValidator;
import com.github.testtemplate.DefaultTestTemplateBuilder;
import com.github.testtemplate.DefaultTestTemplateGivenBuilder;
import com.github.testtemplate.DefaultTestTemplatePreBuilder;
import com.github.testtemplate.DefaultTestValidatorBuilder;
import com.github.testtemplate.TestSuiteFactory;
import com.github.testtemplate.TestTemplateBuilder;
import com.github.testtemplate.TestTemplatePreBuilder;
import com.github.testtemplate.core.TestDefinition;
import com.github.testtemplate.core.TestInstance;
import com.github.testtemplate.core.TestModifier;
import com.github.testtemplate.core.TestVariable;
import com.github.testtemplate.core.listener.DisabledTestListener;
import com.github.testtemplate.core.listener.LoggerListener;
import com.github.testtemplate.core.listener.PreloadVariablesListener;
import com.github.testtemplate.core.runner.TestRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.testtemplate.TestType.ALTERNATIVE;
import static com.github.testtemplate.TestType.DEFAULT;
import static java.util.Collections.emptyList;

public final class TestBuilder {

  private TestBuilder() {
  }

  public static <S> TestTemplatePreBuilder<S> builder(TestSuiteFactory<S> factory) {
    return new InnerTestTemplatePreBuilder<>(factory);
  }

  private static final class InnerTestTemplatePreBuilder<S> implements TestTemplatePreBuilder<S> {

    private final TestSuiteFactory<S> factory;

    private final Map<String, Object> globalAttributes = new HashMap<>();

    private InnerTestTemplatePreBuilder(TestSuiteFactory<S> factory) {
      this.factory = factory;
    }

    @Override
    public TestTemplatePreBuilder<S> disabledAll(String reason) {
      globalAttributes.put(DisabledTestListener.ATTRIBUTE_TEST_DISABLED, true);
      globalAttributes.put(DisabledTestListener.ATTRIBUTE_TEST_DISABLED_REASON, reason);
      return this;
    }

    @Override
    public DefaultTestTemplatePreBuilder<S> defaultTest(String name) {
      return new InnerDefaultTestTemplateBuilder<>(factory, name, globalAttributes);
    }
  }

  private static final class InnerTestTemplateBuilder<S, R> implements TestTemplateBuilder<S, R> {

    private final TestSuiteFactory<S> factory;

    private final ContextualTemplate<R> template;

    private final Map<String, TestVariable> variables;

    private final Map<String, Object> attributes;

    private final List<TestDefinition<R>> tests = new ArrayList<>();

    private InnerTestTemplateBuilder(
        TestSuiteFactory<S> factory,
        ContextualTemplate<R> template,
        Map<String, TestVariable> variables,
        Map<String, Object> attributes) {
      this.factory = factory;
      this.template = template;
      this.variables = variables;
      this.attributes = attributes;
    }

    @Override
    public AlternativeTestTemplatePreBuilder<S, R> test(String name) {
      return new InnerAlternativeTestTemplateBuilder<>(this, name, attributes);
    }

    @Override
    public S suite() {
      var listeners = List.of(new DisabledTestListener(), new LoggerListener(), new PreloadVariablesListener());
      var runner = new TestRunner(listeners);
      var instances = tests.stream().map(t -> new TestInstance<>(t, runner)).collect(Collectors.toList());
      return factory.getSuite(instances);
    }
  }

  private static final class InnerDefaultTestTemplateBuilder<S>
      implements DefaultTestTemplatePreBuilder<S>, DefaultTestTemplateBuilder<S> {

    private final TestSuiteFactory<S> factory;

    private final String name;

    private final Map<String, TestVariable> variables = new LinkedHashMap<>();

    private final Map<String, Object> attributes = new HashMap<>();

    private final Map<String, Object> globalAttributes;

    private InnerDefaultTestTemplateBuilder(
        TestSuiteFactory<S> factory,
        String name,
        Map<String, Object> globalAttributes) {
      this.factory = factory;
      this.name = name;
      this.globalAttributes = globalAttributes;
      attributes.putAll(globalAttributes);
    }

    @Override
    public DefaultTestTemplatePreBuilder<S> disabled(String reason) {
      attributes.put(DisabledTestListener.ATTRIBUTE_TEST_DISABLED, true);
      attributes.put(DisabledTestListener.ATTRIBUTE_TEST_DISABLED_REASON, reason);
      return this;
    }

    @Override
    public DefaultTestTemplateGivenBuilder<S> given(String variable) {
      return new InnerGivenBuilder(variable);
    }

    @Override
    public <R> DefaultTestValidatorBuilder<S, R> when(ContextualTemplate<R> template) {
      return new InnerDefaultTestValidatorBuilder<>(
          factory,
          name,
          template,
          variables,
          attributes,
          globalAttributes);
    }

    private final class InnerGivenBuilder implements DefaultTestTemplateGivenBuilder<S> {

      private final String variable;

      private final Map<String, Object> metadata = new HashMap<>();

      private InnerGivenBuilder(String variable) {
        this.variable = variable;
      }

      @Override
      public DefaultTestTemplateGivenBuilder<S> metadata(String key, Object value) {
        metadata.put(key, value);
        return this;
      }

      @Override
      public DefaultTestTemplateGivenBuilder<S> preload() {
        return metadata(PreloadVariablesListener.METADATA_PRELOAD_VARIABLE, true);
      }

      @Override
      public DefaultTestTemplateBuilder<S> is(Function<Context, ?> value) {
        if (variables.containsKey(variable)) {
          throw new TestBuilderException("The variable '" + variable + "' is already defined");
        }
        variables.put(variable, new TestVariable(variable, value, metadata));
        return InnerDefaultTestTemplateBuilder.this;
      }

      @Override
      public <M extends Extension<S>> M as(ExtensionFactory<S, M> factory) {
        return factory.getExtension(this, variable);
      }
    }
  }

  private static final class InnerDefaultTestValidatorBuilder<S, R> implements DefaultTestValidatorBuilder<S, R> {

    private final TestSuiteFactory<S> factory;

    private final String name;

    private final ContextualTemplate<R> template;

    private final Map<String, TestVariable> variables;

    private final Map<String, Object> attributes;

    private final Map<String, Object> globalAttributes;

    private InnerDefaultTestValidatorBuilder(
        TestSuiteFactory<S> factory,
        String name,
        ContextualTemplate<R> template,
        Map<String, TestVariable> variables,
        Map<String, Object> attributes,
        Map<String, Object> globalAttributes) {
      this.factory = factory;
      this.name = name;
      this.template = template;
      this.variables = variables;
      this.attributes = attributes;
      this.globalAttributes = globalAttributes;
    }

    @Override
    public TestTemplateBuilder<S, R> then(ContextualValidator<R> validator) {
      var builder = new InnerTestTemplateBuilder<>(factory, template, variables, globalAttributes);
      builder.tests.add(new TestDefinition<R>(
          name,
          DEFAULT,
          template,
          variables.values(),
          emptyList(),
          validator,
          attributes));
      return builder;
    }
  }

  private static final class InnerAlternativeTestTemplateBuilder<S, R>
      implements AlternativeTestTemplatePreBuilder<S, R>, AlternativeTestTemplateBuilder<S, R> {

    private final InnerTestTemplateBuilder<S, R> builder;

    private final String name;

    private final Map<String, Object> attributes = new HashMap<>();

    private InnerAlternativeTestTemplateBuilder(
        InnerTestTemplateBuilder<S, R> builder,
        String name,
        Map<String, Object> attributes) {
      this.builder = builder;
      this.name = name;
      this.attributes.putAll(attributes);
    }

    @Override
    public AlternativeTestTemplatePreBuilder<S, R> disabled(String reason) {
      attributes.put(DisabledTestListener.ATTRIBUTE_TEST_DISABLED, true);
      attributes.put(DisabledTestListener.ATTRIBUTE_TEST_DISABLED_REASON, reason);
      return this;
    }

    @Override
    public AlternativeTestValidatorBuilder<S, R> sameAsDefault() {
      return new InnerAlternativeTestValidatorBuilder<>(builder, name, attributes);
    }
  }

  private static final class InnerAlternativeTestValidatorBuilder<S, R>
      implements AlternativeTestValidatorBuilder<S, R> {

    private final InnerTestTemplateBuilder<S, R> builder;

    private final String name;

    private final Map<String, TestModifier> modifiers = new LinkedHashMap<>();

    private final Map<String, Object> attributes;

    private InnerAlternativeTestValidatorBuilder(
        InnerTestTemplateBuilder<S, R> builder,
        String name,
        Map<String, Object> attributes) {
      this.builder = builder;
      this.name = name;
      this.attributes = attributes;
    }

    @Override
    public AlternativeTestTemplateExceptBuilder<S, R> except(String variable) {
      return new InnerExceptBuilder(variable);
    }

    @Override
    public TestTemplateBuilder<S, R> then(ContextualValidator<R> validator) {
      builder.tests.add(new TestDefinition<>(
          name,
          ALTERNATIVE,
          builder.template,
          builder.variables.values(),
          modifiers.values(),
          validator,
          attributes));
      return builder;
    }

    private final class InnerExceptBuilder implements AlternativeTestTemplateExceptBuilder<S, R> {

      private final String variable;

      private final Map<String, Object> metadata = new HashMap<>();

      private InnerExceptBuilder(String variable) {
        this.variable = variable;

        Optional
            .ofNullable(builder.variables.get(variable))
            .map(TestVariable::getMetadata)
            .ifPresent(metadata::putAll);
      }

      @Override
      public AlternativeTestTemplateExceptBuilder<S, R> metadata(String key, Object value) {
        metadata.put(key, value);
        return this;
      }

      @Override
      public AlternativeTestValidatorBuilder<S, R> is(Function<ContextView, ?> value) {
        if (modifiers.containsKey(variable)) {
          throw new TestBuilderException("The modifier '" + variable + "' is already defined");
        }

        modifiers.put(variable, new TestModifier(variable, value, metadata));
        return InnerAlternativeTestValidatorBuilder.this;
      }

      @Override
      public <M extends Extension<S, R>> M as(ExtensionFactory<S, R, M> factory) {
        return factory.getExtension(this, variable);
      }
    }
  }
}
