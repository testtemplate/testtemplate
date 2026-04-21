package io.github.testtemplate.core.builder;

import io.github.testtemplate.api.suite.TestSuiteFactory;
import io.github.testtemplate.core.TestDefinition;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CoreBuilderTest {

  private final TestInstantiator testInstantiator = new WrapTestInstantiator();
  private final TestSuiteFactory<List<TestDefinition<?>>> testFactory = new WrapTestSuiteFactory();

  @Nested
  class DefaultTestTest {

    @Test
    void shouldCreateDefaultTestWithName() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("my test")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .isDefaultTest()
          .hasName("my test");
    }

    @Test
    void shouldRegisterGivenVariableInDefaultTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .given("x").is("value")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasVariableWithValue("x", "value");
    }

    @Test
    void shouldRegisterMultipleGivenVariablesInDefaultTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .given("x").is("value")
          .given("y").is("other")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasVariableWithValue("x", "value")
          .hasVariableWithValue("y", "other");
    }

    @Test
    void shouldRegisterMetadataInDefaultTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .metadata("key", "value")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasMetadata("key", "value");
    }

    @Test
    void shouldThrowWhenDuplicateVariableDeclared() {
      Assertions
          .assertThatThrownBy(() ->
              CoreBuilder.builder(testFactory, testInstantiator)
                  .defaultTest("test")
                  .given("x").is("first")
                  .given("x").is("second"))
          .isInstanceOf(TestBuilderException.class)
          .hasMessage("The variable 'x' is already defined");
    }

    @Test
    void shouldCreateDefaultTestWithNoModifiersAndNoParameters() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasNoModifiers()
          .hasNoParameters();
    }
  }

  @Nested
  class AlternativeTestTest {

    @Test
    void shouldCreateAlternativeTestWithName() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .isAlternativeTest()
          .hasName("alt");
    }

    @Test
    void shouldInheritVariablesFromDefaultTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("value")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasVariableWithValue("x", "value");
    }

    @Test
    void shouldRegisterModifierWhenExceptWithSingleValue() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("original")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("override")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasModifierWithValue("x", "override")
          .hasNoParameters();
    }

    @Test
    void shouldRegisterParameterWhenExceptWithMultipleValues() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("original")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("v1").or("v2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x", "v1", "v2")
          .hasNoModifiers();
    }

    @Test
    void shouldRegisterModifiersWhenExceptWithTwoVariablesSingleRow() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1")
          .given("y").is("y1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x", "y").are("x2", "y2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasModifierWithValue("x", "x2")
          .hasModifierWithValue("y", "y2")
          .hasNoParameters();
    }

    @Test
    void shouldRegisterParametersWhenExceptWithTwoVariablesMultipleRows() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1")
          .given("y").is("y1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x", "y")
              .are("x2", "y2")
              .or("x3", "y3")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x|y", "x2", "x3")
          .hasParameterWithValues("y", "x|y", "y2", "y3")
          .hasNoModifiers();
    }

    @Test
    void shouldRegisterTwoIndependentParameterGroups() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1")
          .given("y").is("y1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("x2").or("x3")
          .except("y").is("y2").or("y3")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x", "x2", "x3")
          .hasParameterWithValues("y", "y", "y2", "y3");
    }

    @Test
    void shouldAccumulateMultipleAlternativeTests() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt1").sameAsDefault().then(ctx -> {})
          .test("alt2").sameAsDefault().then(ctx -> {})
          .suite();

      Assertions.assertThat(definitions).hasSize(3);
    }

    @Test
    void shouldRegisterMetadataInAlternativeTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .metadata("key", "value")
          .sameAsDefault()
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasMetadata("key", "value");
    }
  }

  @Nested
  class SetupTest {

    @Test
    void shouldRegisterVariableDefinedInSetUp() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .setUp(setup -> setup.given("x").is("setup-value"))
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasVariableWithValue("x", "setup-value");
    }

    @Test
    void shouldRegisterMetadataDefinedInSetUp() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .setUp(setup -> setup.metadata("key", "setup-meta"))
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasMetadata("key", "setup-meta");
    }

    @Test
    void shouldThrowWhenSetUpLambdaThrows() {
      Assertions
          .assertThatThrownBy(() ->
              CoreBuilder.builder(testFactory, testInstantiator)
                  .defaultTest("test")
                  .setUp(setup -> { throw new RuntimeException("boom"); }))
          .isInstanceOf(TestBuilderException.class)
          .hasMessage("Caught exception");
    }

    @Test
    void shouldRegisterMultipleVariablesDefinedInSetUp() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .setUp(setup -> setup.given("x").is("x-value").given("y").is("y-value"))
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasVariableWithValue("x", "x-value")
          .hasVariableWithValue("y", "y-value");
    }

    @Test
    void shouldRegisterVariableMetadataDefinedInSetUp() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .setUp(setup -> setup.given("x").metadata("meta-key", "meta-value").is("x-value"))
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      var variable = definitions.getFirst().getVariables().getFirst();
      Assertions.assertThat(variable.getMetadata().get("meta-key")).isEqualTo("meta-value");
    }
  }

  @Nested
  class VariableMetadataTest {

    @Test
    void shouldRegisterVariableMetadataInDefaultTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .given("x").metadata("meta-key", "meta-value").is("value")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      var variable = definitions.getFirst().getVariables().getFirst();
      Assertions.assertThat(variable.getMetadata().get("meta-key")).isEqualTo("meta-value");
    }

    @Test
    void shouldRegisterVariableMetadataInAlternativeTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("original")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").metadata("meta-key", "meta-value").is("override")
          .then(ctx -> {})
          .suite();

      var modifier = definitions.get(1).getModifiers().getFirst();
      Assertions.assertThat(modifier.getMetadata().get("meta-key")).isEqualTo("meta-value");
    }
  }

  @Nested
  class ExceptChainTest {

    @Test
    void shouldRegisterModifiersWhenExceptWithThreeVariables() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1").given("z").is("z1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x", "y", "z").are("x2", "y2", "z2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasModifierWithValue("x", "x2")
          .hasModifierWithValue("y", "y2")
          .hasModifierWithValue("z", "z2");
    }

    @Test
    void shouldRegisterModifiersWhenExceptWithFourVariables() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b", "c", "d").are("a2", "b2", "c2", "d2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasModifierWithValue("a", "a2")
          .hasModifierWithValue("b", "b2")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2");
    }

    @Test
    void shouldRegisterModifiersWhenExceptWithFiveVariables() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1").given("e").is("e1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b", "c", "d", "e").are("a2", "b2", "c2", "d2", "e2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasModifierWithValue("a", "a2")
          .hasModifierWithValue("b", "b2")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2")
          .hasModifierWithValue("e", "e2");
    }

    @Test
    void shouldRegisterModifiersWhenExceptWithVariableList() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except(List.of("x", "y"))
          .are(List.of(ctx -> "x2", ctx -> "y2"))
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasModifierWithValue("x", "x2")
          .hasModifierWithValue("y", "y2");
    }

    @Test
    void shouldChainSecondExceptAfterSingleValuePostStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("x2")
          .except("y").is("y2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasModifierWithValue("x", "x2")
          .hasModifierWithValue("y", "y2");
    }

    @Test
    void shouldChainTwoVariableExceptAfterParameterPostStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1").given("z").is("z1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("x2").or("x3")
          .except("y", "z").are("y2", "z2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x", "x2", "x3")
          .hasModifierWithValue("y", "y2")
          .hasModifierWithValue("z", "z2");
    }

    @Test
    void shouldChainThreeVariableExceptAfterParameterPostStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1").given("z").is("z1").given("w").is("w1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("x2").or("x3")
          .except("y", "z", "w").are("y2", "z2", "w2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x", "x2", "x3")
          .hasModifierWithValue("y", "y2")
          .hasModifierWithValue("z", "z2")
          .hasModifierWithValue("w", "w2");
    }

    @Test
    void shouldChainFourVariableExceptAfterParameterPostStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("x2").or("x3")
          .except("a", "b", "c", "d").are("a2", "b2", "c2", "d2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert.assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x", "x2", "x3")
          .hasModifierWithValue("a", "a2")
          .hasModifierWithValue("b", "b2")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2");
    }

    @Test
    void shouldChainFiveVariableExceptAfterParameterPostStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1")
          .given("a").is("a1")
          .given("b").is("b1")
          .given("c").is("c1")
          .given("d").is("d1")
          .given("e").is("e1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x").is("x2").or("x3")
          .except("a", "b", "c", "d", "e").are("a2", "b2", "c2", "d2", "e2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x", "x2", "x3")
          .hasModifierWithValue("a", "a2")
          .hasModifierWithValue("b", "b2")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2")
          .hasModifierWithValue("e", "e2");
    }

    @Test
    void shouldChainListExceptAfterParameterPostStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1").given("z").is("z1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt").sameAsDefault()
          .except("x").is("x2").or("x3")
          .except(List.of("y", "z"))
          .are(List.of(ctx -> "y2", ctx -> "z2"))
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x", "x2", "x3")
          .hasModifierWithValue("y", "y2")
          .hasModifierWithValue("z", "z2");
    }
  }

  @Nested
  class PostNStepExceptChainTest {

    @Test
    void shouldChainSingleExceptAfterMultiVariableParameterPostNStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1").given("z").is("z1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt").sameAsDefault()
          .except("x", "y").are("x2", "y2").or("x3", "y3")
          .except("z").is("z2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x|y", "x2", "x3")
          .hasParameterWithValues("y", "x|y", "y2", "y3")
          .hasModifierWithValue("z", "z2");
    }

    @Test
    void shouldChainTwoVariableExceptAfterMultiVariableParameterPostNStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b").are("a2", "b2").or("a3", "b3")
          .except("c", "d").are("c2", "d2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("a", "a|b", "a2", "a3")
          .hasParameterWithValues("b", "a|b", "b2", "b3")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2");
    }

    @Test
    void shouldChainThreeVariableExceptAfterMultiVariableParameterPostNStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1").given("e").is("e1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b").are("a2", "b2").or("a3", "b3")
          .except("c", "d", "e").are("c2", "d2", "e2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("a", "a|b", "a2", "a3")
          .hasParameterWithValues("b", "a|b", "b2", "b3")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2")
          .hasModifierWithValue("e", "e2");
    }

    @Test
    void shouldChainFourVariableExceptAfterMultiVariableParameterPostNStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1")
          .given("c").is("c1").given("d").is("d1").given("e").is("e1").given("f").is("f1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b").are("a2", "b2").or("a3", "b3")
          .except("c", "d", "e", "f").are("c2", "d2", "e2", "f2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("a", "a|b", "a2", "a3")
          .hasParameterWithValues("b", "a|b", "b2", "b3")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2")
          .hasModifierWithValue("e", "e2")
          .hasModifierWithValue("f", "f2");
    }

    @Test
    void shouldChainFiveVariableExceptAfterMultiVariableParameterPostNStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1")
          .given("c").is("c1").given("d").is("d1").given("e").is("e1").given("f").is("f1").given("g").is("g1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b").are("a2", "b2").or("a3", "b3")
          .except("c", "d", "e", "f", "g").are("c2", "d2", "e2", "f2", "g2")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("a", "a|b", "a2", "a3")
          .hasParameterWithValues("b", "a|b", "b2", "b3")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2")
          .hasModifierWithValue("e", "e2")
          .hasModifierWithValue("f", "f2")
          .hasModifierWithValue("g", "g2");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldChainListExceptAfterMultiVariableParameterPostNStep() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b").are("a2", "b2").or("a3", "b3")
          .except(List.of("c", "d"))
          .are(List.of(ctx -> "c2", ctx -> "d2"))
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("a", "a|b", "a2", "a3")
          .hasParameterWithValues("b", "a|b", "b2", "b3")
          .hasModifierWithValue("c", "c2")
          .hasModifierWithValue("d", "d2");
    }
  }

  @Nested
  class MultiVariableOrTest {

    @Test
    void shouldRegisterParametersWhenExceptWithThreeVariablesMultipleRows() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("x").is("x1").given("y").is("y1").given("z").is("z1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("x", "y", "z").are("x2", "y2", "z2").or("x3", "y3", "z3")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.get(1))
          .hasParameterWithValues("x", "x|y|z", "x2", "x3")
          .hasParameterWithValues("y", "x|y|z", "y2", "y3")
          .hasParameterWithValues("z", "x|y|z", "z2", "z3");
    }

    @Test
    void shouldRegisterParametersWhenExceptWithFourVariablesMultipleRows() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b", "c", "d").are("a2", "b2", "c2", "d2").or("a3", "b3", "c3", "d3")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert.assertThat(definitions.get(1))
          .hasParameterWithValues("a", "a|b|c|d", "a2", "a3")
          .hasParameterWithValues("b", "a|b|c|d", "b2", "b3")
          .hasParameterWithValues("c", "a|b|c|d", "c2", "c3")
          .hasParameterWithValues("d", "a|b|c|d", "d2", "d3");
    }

    @Test
    void shouldRegisterParametersWhenExceptWithFiveVariablesMultipleRows() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .given("a").is("a1").given("b").is("b1").given("c").is("c1").given("d").is("d1").given("e").is("e1")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .except("a", "b", "c", "d", "e").are("a2", "b2", "c2", "d2", "e2").or("a3", "b3", "c3", "d3", "e3")
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert.assertThat(definitions.get(1))
          .hasParameterWithValues("a", "a|b|c|d|e", "a2", "a3")
          .hasParameterWithValues("b", "a|b|c|d|e", "b2", "b3")
          .hasParameterWithValues("c", "a|b|c|d|e", "c2", "c3")
          .hasParameterWithValues("d", "a|b|c|d|e", "d2", "d3")
          .hasParameterWithValues("e", "a|b|c|d|e", "e2", "e3");
    }
  }

  @Nested
  class ValidationTest {

    @Test
    void shouldThrowWhenAreListSizeDoesNotMatchVariableCount() {
      Assertions
          .assertThatThrownBy(() ->
              CoreBuilder.builder(testFactory, testInstantiator)
                  .defaultTest("default")
                  .given("x").is("x1").given("y").is("y1")
                  .when(ctx -> null)
                  .then(ctx -> {})
                  .test("alt")
                  .sameAsDefault()
                  .except(List.of("x", "y"))
                  .are(List.of(ctx -> "x2")))
          .isInstanceOf(TestBuilderException.class)
          .hasMessage("Expecting 2 values");
    }

    @Test
    void shouldThrowWhenOrListSizeDoesNotMatchVariableCount() {
      Assertions
          .assertThatThrownBy(() ->
              CoreBuilder.builder(testFactory, testInstantiator)
                  .defaultTest("default")
                  .given("x").is("x1").given("y").is("y1")
                  .when(ctx -> null)
                  .then(ctx -> {})
                  .test("alt")
                  .sameAsDefault()
                  .except(List.of("x", "y"))
                  .are(List.of(ctx -> "x2", ctx -> "y2"))
                  .or(List.of(ctx -> "x3")))
          .isInstanceOf(TestBuilderException.class)
          .hasMessage("Expecting 2 values");
    }
  }

  @Nested
  class ValidatorSourceTest {

    @Test
    void shouldCaptureValidatorSourceForDefaultTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      TestDefinitionAssert
          .assertThat(definitions.getFirst())
          .hasValidatorSource();
    }

    @Test
    void shouldCaptureValidatorSourceForAlternativeTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .then(ctx -> {})
          .suite();

      var defaultSource = definitions.getFirst().getValidator().getSource();
      var altSource = definitions.get(1).getValidator().getSource();
      Assertions.assertThat(altSource).isNotNull();
      Assertions.assertThat(altSource.getFileName()).isNotNull();
      Assertions.assertThat(altSource.getLineNumber()).isGreaterThan(0);
      Assertions.assertThat(defaultSource).isNotNull();
    }

    @Test
    void shouldNotCaptureCoreBuilderFrameAsValidatorSource() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      var source = definitions.getFirst().getValidator().getSource();
      Assertions.assertThat(source.getClassName()).doesNotStartWith("io.github.testtemplate.");
    }
  }

  @Nested
  class TemplateSourceTest {

    @Test
    void shouldCaptureTemplateSourceForDefaultTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      var source = definitions.getFirst().getTemplate().getSource();
      Assertions.assertThat(source).isNotNull();
      Assertions.assertThat(source.getFileName()).isNotNull();
      Assertions.assertThat(source.getLineNumber()).isGreaterThan(0);
    }

    @Test
    void shouldCaptureTemplateSourceForAlternativeTest() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("default")
          .when(ctx -> null)
          .then(ctx -> {})
          .test("alt")
          .sameAsDefault()
          .then(ctx -> {})
          .suite();

      var defaultSource = definitions.getFirst().getTemplate().getSource();
      var altSource = definitions.get(1).getTemplate().getSource();
      Assertions.assertThat(altSource).isNotNull();
      Assertions.assertThat(altSource.getFileName()).isEqualTo(defaultSource.getFileName());
      Assertions.assertThat(altSource.getLineNumber()).isEqualTo(defaultSource.getLineNumber());
    }

    @Test
    void shouldNotCaptureCoreBuilderFrameAsTemplateSource() {
      var definitions = CoreBuilder.builder(testFactory, testInstantiator)
          .defaultTest("test")
          .when(ctx -> null)
          .then(ctx -> {})
          .suite();

      var source = definitions.getFirst().getTemplate().getSource();
      Assertions.assertThat(source.getClassName()).doesNotStartWith("io.github.testtemplate.");
    }
  }

  private static final class WrapTestSuiteFactory implements TestSuiteFactory<List<TestDefinition<?>>> {

    @Override
    public List<TestDefinition<?>> getSuite(final Stream<? extends Test> tests) {
      return tests
          .map(t -> (TestDefinitionWrapper<?>) t)
          .map(TestDefinitionWrapper::getDefinition)
          .collect(Collectors.toUnmodifiableList());
    }
  }

  private static final class WrapTestInstantiator implements TestInstantiator {

    @Override
    public <R> TestDefinitionWrapper<R> instantiate(final TestDefinition<R> test) {
      return new TestDefinitionWrapper<>(test);
    }
  }

  private static final class TestDefinitionWrapper<R> implements TestSuiteFactory.TestItem {

    private final TestDefinition<R> definition;

    private TestDefinitionWrapper(TestDefinition<R> definition) {
      this.definition = definition;
    }

    @Override
    public String getName() {
      return definition.getName();
    }

    @Override
    public void execute() {}

    private TestDefinition<?> getDefinition() {
      return definition;
    }
  }
}
