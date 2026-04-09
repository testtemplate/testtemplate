package io.github.testtemplate.core.builder;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.TestType;
import io.github.testtemplate.core.TestDefinition;

public class TestDefinitionAssert extends AbstractAssert<TestDefinitionAssert, TestDefinition<?>> {

  public TestDefinitionAssert(TestDefinition testDefinition) {
    super(testDefinition, TestDefinitionAssert.class);
  }

  public TestDefinitionAssert isDefaultTest() {
    isNotNull();
    Assertions.assertThat(actual.getType()).as("verify test type").isEqualTo(TestType.DEFAULT);
    return this;
  }

  public TestDefinitionAssert isAlternativeTest() {
    isNotNull();
    Assertions.assertThat(actual.getType()).as("verify test type").isEqualTo(TestType.ALTERNATIVE);
    return this;
  }

  public TestDefinitionAssert hasName(String name) {
    isNotNull();
    Assertions.assertThat(actual.getName()).as("verifying test name").isEqualTo(name);
    return this;
  }

  public TestDefinitionAssert hasVariable(String name) {
    isNotNull();
    actual.getVariables()
        .stream()
        .filter(v -> v.getName().equals(name))
        .findFirst()
        .orElseThrow(AssertionError::new);
    return this;
  }

  public TestDefinitionAssert hasVariableWithValue(String name, @Nullable Object expectedValue) {
    isNotNull();
    var variable = actual.getVariables()
        .stream()
        .filter(v -> v.getName().equals(name))
        .findFirst()
        .orElseThrow(AssertionError::new);
    try {
      var actualValue = variable.getValueSupplier().apply(null);
      Assertions.assertThat(actualValue).as("verifying value of variable '%s'", name).isEqualTo(expectedValue);
    } catch (AssertionError e) {
      throw e;
    } catch (Throwable e) {
      throw new AssertionError("Unexpected exception evaluating variable '" + name + "'", e);
    }
    return this;
  }

  public TestDefinitionAssert hasNoVariables() {
    isNotNull();
    Assertions.assertThat(actual.getVariables()).as("verifying no variables").isEmpty();
    return this;
  }

  public TestDefinitionAssert hasModifier(String name) {
    isNotNull();
    actual.getModifiers()
        .stream()
        .filter(m -> m.getName().equals(name))
        .findFirst()
        .orElseThrow(AssertionError::new);
    return this;
  }

  public TestDefinitionAssert hasModifierWithValue(String name, @Nullable Object expectedValue) {
    isNotNull();
    var modifier = actual.getModifiers()
        .stream()
        .filter(m -> m.getName().equals(name))
        .findFirst()
        .orElseThrow(AssertionError::new);
    try {
      var actualValue = modifier.getValueSupplier().apply(null);
      Assertions.assertThat(actualValue).as("verifying value of modifier '%s'", name).isEqualTo(expectedValue);
    } catch (AssertionError e) {
      throw e;
    } catch (Throwable e) {
      throw new AssertionError("Unexpected exception evaluating modifier '" + name + "'", e);
    }
    return this;
  }

  public TestDefinitionAssert hasNoModifiers() {
    isNotNull();
    Assertions.assertThat(actual.getModifiers()).as("verifying no modifiers").isEmpty();
    return this;
  }

  public TestDefinitionAssert hasParameter(String name) {
    isNotNull();
    actual.getParameters()
        .stream()
        .filter(p -> p.getName().equals(name))
        .findFirst()
        .orElseThrow(AssertionError::new);
    return this;
  }

  public TestDefinitionAssert hasParameterWithValues(
      String name,
      String expectedGroup,
      @Nullable Object... expectedValues) {
    isNotNull();
    var parameter = actual.getParameters()
        .stream()
        .filter(p -> p.getName().equals(name))
        .findFirst()
        .orElseThrow(AssertionError::new);
    Assertions.assertThat(parameter.getGroup()).as("verifying group of parameter '%s'", name).isEqualTo(expectedGroup);
    try {
      var actualValues = new java.util.ArrayList<>();
      for (var supplier : parameter.getValueSuppliers()) {
        actualValues.add(supplier.apply(null));
      }
      Assertions.assertThat(actualValues)
          .as("verifying values of parameter '%s'", name)
          .containsExactly(expectedValues);
    } catch (AssertionError e) {
      throw e;
    } catch (Throwable e) {
      throw new AssertionError("Unexpected exception evaluating parameter '" + name + "'", e);
    }
    return this;
  }

  public TestDefinitionAssert hasNoParameters() {
    isNotNull();
    Assertions.assertThat(actual.getParameters()).as("verifying no parameters").isEmpty();
    return this;
  }

  public TestDefinitionAssert hasMetadata(String key, @Nullable Object value) {
    isNotNull();
    Assertions.assertThat(actual.getMetadata().get(key)).as("verifying metadata key '%s'", key).isEqualTo(value);
    return this;
  }

  public TestDefinitionAssert hasValidatorSource() {
    isNotNull();
    var source = actual.getValidator().getSource();
    Assertions.assertThat(source).as("verifying validator source is not null").isNotNull();
    Assertions.assertThat(source.getFileName()).as("verifying validator source file name is not null").isNotNull();
    Assertions.assertThat(source.getLineNumber())
        .as("verifying validator source line number is positive")
        .isGreaterThan(0);
    return this;
  }

  public TestDefinitionAssert hasValidatorSourceClassName(String expectedClassName) {
    isNotNull();
    var source = actual.getValidator().getSource();
    Assertions.assertThat(source).as("verifying validator source is not null").isNotNull();
    Assertions.assertThat(source.getClassName())
        .as("verifying validator source class name")
        .isEqualTo(expectedClassName);
    return this;
  }

  public static TestDefinitionAssert assertThat(TestDefinition<?> actual) {
    return new TestDefinitionAssert(actual);
  }
}
