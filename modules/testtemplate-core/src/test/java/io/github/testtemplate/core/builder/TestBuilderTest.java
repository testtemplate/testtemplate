package io.github.testtemplate.core.builder;

import io.github.testtemplate.AlternativeTestTemplateExceptBuilder;
import io.github.testtemplate.AlternativeTestValidatorBuilder;
import io.github.testtemplate.ContextualTemplate;
import io.github.testtemplate.ContextualValidator;
import io.github.testtemplate.DefaultTestTemplateBuilder;
import io.github.testtemplate.DefaultTestTemplateGivenBuilder;
import io.github.testtemplate.TestSuiteFactory;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TestBuilderTest {

  private static final ContextualTemplate<Object> NO_OP_TEMPLATE = c -> null;
  private static final ContextualValidator<Object> NO_OP_VALIDATOR = c -> {};

  @Test
  void buildSimpleDefaultTest() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())

        .defaultTest("default")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    builder
        .test("alternative 1")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(c -> assertThat(c.result()).isEqualTo("hi"));

    builder
        .test("alternative 2")
        .sameAsDefault()
        .except("greeting").is(c -> c.get("greeting") + " bob")
        .then(c -> assertThat(c.result()).isEqualTo("hello obo"));

    var result = execute(builder.suite());

    assertThat(result)
        .extracting("name", "type")
        .containsExactly(
            Tuple.tuple("default", TestResultType.SUCCESS),
            Tuple.tuple("alternative 1", TestResultType.SUCCESS),
            Tuple.tuple("alternative 2", TestResultType.FAILURE));
  }

  @Test
  void buildSimpleAlternativeTests() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())

        .defaultTest("simple default test")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    var result = execute(builder.suite());

    assertThat(result)
        .extracting("name", "type")
        .containsExactly(tuple("simple default test", TestResultType.SUCCESS));
  }

  @Test
  void buildShouldAllowExtensionForGivenVariables() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())
        .defaultTest("simple default test with extension")
        .given("greeting").as(simpleGivenExtension()).isSomethingSpecial()
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("I am a potato!"));

    var result = execute(builder.suite());

    assertThat(result)
        .extracting("name", "type")
        .containsExactly(tuple("simple default test with extension", TestResultType.SUCCESS));
  }

  @Test
  void buildShouldAllowExtensionForExceptVariables() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())
        .defaultTest("simple default")
        .given("greeting").is("I am a potato.")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("I am a potato."));

    builder
        .test("alternative with extension")
        .sameAsDefault()
        .except("greeting").as(simpleExceptExtension()).addSomethingSpecial()
        .then(c -> assertThat(c.result()).isEqualTo("Super! I am a potato."));

    var result = execute(builder.suite());

    assertThat(result)
        .extracting("name", "type")
        .containsExactly(
            tuple("simple default", TestResultType.SUCCESS),
            tuple("alternative with extension", TestResultType.SUCCESS));
  }

  @Test
  void buildShouldThrowExceptionWhenVariableIsDefinedTwice() {
    Assertions
        .assertThatThrownBy(() -> TestBuilder.builder(new SimpleTestSuiteFactory())
            .defaultTest("simple default test")
            .given("greeting").is("hello")
            .given("greeting").is("bonjour") // will throw
            .when(c -> c.get("greeting"))
            .then(c -> assertThat(c.result()).isEqualTo("hello")))
        .isInstanceOf(TestBuilderException.class)
        .hasMessage("The variable 'greeting' is already defined");
  }

  @Test
  void buildShouldThrowExceptionWhenModifierIsDefinedTwice() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())

        .defaultTest("default")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    Assertions
        .assertThatThrownBy(() -> builder
            .test("alternative 1")
            .sameAsDefault()
            .except("greeting").is("hi")
            .except("greeting").is("hello") // will throw
            .then(c -> assertThat(c.result()).isEqualTo("hi")))
        .isInstanceOf(TestBuilderException.class)
        .hasMessage("The modifier 'greeting' is already defined");
  }

  @Test
  void disableDefaultTestShouldNotExecuteDefaultTest() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())

        .defaultTest("default")
        .disabled("this is a test")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    builder
        .test("alternative 1")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(c -> assertThat(c.result()).isEqualTo("hi"));

    builder
        .test("alternative 2")
        .sameAsDefault()
        .except("greeting").is(c -> c.get("greeting") + " bob")
        .then(c -> assertThat(c.result()).isEqualTo("hello obo"));

    var result = execute(builder.suite());

    assertThat(result)
        .extracting("name", "type")
        .containsExactly(
            tuple("default", TestResultType.SKIPPED),
            tuple("alternative 1", TestResultType.SUCCESS),
            tuple("alternative 2", TestResultType.FAILURE));
  }

  @Test
  void disableAlternativeTestShouldNotExecuteSpecifiedTest() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())

        .defaultTest("default")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    builder
        .test("alternative 1")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(c -> assertThat(c.result()).isEqualTo("hi"));

    builder
        .test("alternative 2")
        .disabled("this one doesn't work")
        .sameAsDefault()
        .except("greeting").is(c -> c.get("greeting") + " bob")
        .then(c -> assertThat(c.result()).isEqualTo("hello obo"));

    var result = execute(builder.suite());

    assertThat(result)
        .extracting("name", "type")
        .containsExactly(
            tuple("default", TestResultType.SUCCESS),
            tuple("alternative 1", TestResultType.SUCCESS),
            tuple("alternative 2", TestResultType.SKIPPED));
  }

  @Test
  void disabledAllShouldNotExecuteAnyTest() {
    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())
        .disabledAll()

        .defaultTest("default")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    builder
        .test("alternative 1")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(c -> assertThat(c.result()).isEqualTo("hi"));

    builder
        .test("alternative 2")
        .sameAsDefault()
        .except("greeting").is(c -> c.get("greeting") + " bob")
        .then(c -> assertThat(c.result()).isEqualTo("hello obo"));

    var result = execute(builder.suite());

    assertThat(result)
        .extracting("name", "type")
        .containsExactly(
            tuple("default", TestResultType.SKIPPED),
            tuple("alternative 1", TestResultType.SKIPPED),
            tuple("alternative 2", TestResultType.SKIPPED));
  }

  @Test
  void preloadShouldLoadVariableEvenIfNotReferenced() {
    var check = new AtomicInteger();

    var builder = TestBuilder
        .builder(new SimpleTestSuiteFactory())
        .defaultTest("default")
        .given("setup").preload().is(c -> check.incrementAndGet())
        .when(NO_OP_TEMPLATE)
        .then(NO_OP_VALIDATOR);

    builder
        .test("alternative 1")
        .sameAsDefault()
        .then(NO_OP_VALIDATOR);

    builder
        .test("alternative 2")
        .sameAsDefault()
        .except("setup").is(c -> check.addAndGet(10))
        .then(NO_OP_VALIDATOR);

    execute(builder.suite());

    assertThat(check.get()).isEqualTo(12);
  }

  private static List<TestResult> execute(List<TestSuiteFactory.Test> tests) {
    List<TestResult> results = new ArrayList<>();
    for (TestSuiteFactory.Test test : tests) {
      try {
        test.execute();
        results.add(new TestResult(test.getName(), TestResultType.SUCCESS));
      } catch (TestAbortedException e) {
        results.add(new TestResult(test.getName(), TestResultType.SKIPPED));
      } catch (Throwable t) {
        results.add(new TestResult(test.getName(), TestResultType.FAILURE, t));
      }
    }
    return results;
  }

  private static <S> SimpleGivenExtensionFactory<S> simpleGivenExtension() {
    return new SimpleGivenExtensionFactory<>();
  }

  private static <S, R> SimpleExceptExtensionFactory<S, R> simpleExceptExtension() {
    return new SimpleExceptExtensionFactory<>();
  }

  private static final class SimpleTestSuiteFactory implements TestSuiteFactory<List<TestSuiteFactory.Test>> {

    @Override
    public List<Test> getSuite(List<? extends Test> tests) {
      return new ArrayList<>(tests);
    }
  }

  private static final class TestResult {
    private final String name;
    private final TestResultType type;
    private final Throwable throwable;

    private TestResult(String name, TestResultType type) {
      this.name = name;
      this.type = type;
      this.throwable = null;
    }

    private TestResult(String name, TestResultType type, Throwable throwable) {
      this.name = name;
      this.type = type;
      this.throwable = throwable;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestResult that = (TestResult) o;
      return Objects.equals(name, that.name) && type == that.type && Objects.equals(throwable, that.throwable);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, type, throwable);
    }

    @Override
    public String toString() {
      return "TestResult{"
          + "name='" + name + '\''
          + ", type=" + type
          + ", throwable=" + throwable
          + '}';
    }
  }

  private enum TestResultType {
    SUCCESS,
    FAILURE,
    SKIPPED
  }

  private static final class SimpleGivenExtensionFactory<S>
      implements DefaultTestTemplateGivenBuilder.ExtensionFactory<S, SimpleGivenExtension<S>> {

    @Override
    public SimpleGivenExtension<S> getExtension(DefaultTestTemplateGivenBuilder<S> builder, String variable) {
      return new SimpleGivenExtension<>(builder);
    }
  }

  private static final class SimpleGivenExtension<S>
      implements DefaultTestTemplateGivenBuilder.Extension<S> {

    private final DefaultTestTemplateGivenBuilder<S> builder;

    private SimpleGivenExtension(DefaultTestTemplateGivenBuilder<S> builder) {
      this.builder = builder;
    }

    public DefaultTestTemplateBuilder<S> isSomethingSpecial() {
      return builder.is("I am a potato!");
    }
  }

  private static final class SimpleExceptExtensionFactory<S, R>
      implements AlternativeTestTemplateExceptBuilder.ExtensionFactory<S, R, SimpleExceptExtension<S, R>> {

    @Override
    public SimpleExceptExtension<S, R> getExtension(
        AlternativeTestTemplateExceptBuilder<S, R> builder,
        String variable) {
      return new SimpleExceptExtension<>(builder, variable);
    }
  }

  private static final class SimpleExceptExtension<S, R>
      implements AlternativeTestTemplateExceptBuilder.Extension<S, R> {

    private final AlternativeTestTemplateExceptBuilder<S, R> builder;

    private final String variable;

    private SimpleExceptExtension(AlternativeTestTemplateExceptBuilder<S, R> builder, String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    public AlternativeTestValidatorBuilder<S, R> addSomethingSpecial() {
      return builder.is(c -> "Super! " + c.get(variable));
    }
  }
}
