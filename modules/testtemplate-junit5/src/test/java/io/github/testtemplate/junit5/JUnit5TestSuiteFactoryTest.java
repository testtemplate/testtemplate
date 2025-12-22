package io.github.testtemplate.junit5;

import io.github.testtemplate.TestSuiteFactory;
import io.github.testtemplate.TestType;
import io.github.testtemplate.core.TestDefinition;
import io.github.testtemplate.core.TestInstance;
import io.github.testtemplate.core.TestParameter;
import io.github.testtemplate.core.builder.TestBuilder;
import io.github.testtemplate.core.runner.TestRunnerFactory;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JUnit5TestSuiteFactoryTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(JUnit5TestSuiteFactoryTest.class);

  @TestFactory
  List<DynamicNode> runWithJUnit5() {
    var builder = TestBuilder.builder(new JUnit5TestSuiteFactory())
        .defaultTest("default")
        .given("greeting").is("hello")
        .when(c -> c.get("greeting"))
        .then(c -> assertThat(c.result()).isEqualTo("hello"));

    builder
        .test("alternative")
        .sameAsDefault()
        .except("greeting").is("hi")
        .then(c -> assertThat(c.result()).isEqualTo("hi"));

    builder
        .test("disabled")
        .disabled()
        .sameAsDefault()
        .then(c -> assertThat(c.result()).isEqualTo("oops disabled"));

    return builder.suite();
  }

  @TestFactory
  List<DynamicNode> testOfGroup() {
    var test = new TestDefinition<Object>(
        "group",
        TestType.ALTERNATIVE,
        c -> 1 + 2,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.singletonList(new TestParameter("var", List.of(c -> "1", c -> "2"))),
        r -> {},
        Map.of());

    var runner = new TestRunnerFactory(List.of());

    var instance = TestInstance.of(test, runner);

    assertThat(instance)
        .isInstanceOf(TestInstance.TestGroupInstance.class)
        .hasFieldOrPropertyWithValue("name", "group");

    assertThat(((TestInstance.TestGroupInstance<?>) instance).getTests()).size().isEqualTo(2);

    JUnit5TestSuiteFactory factory = new JUnit5TestSuiteFactory();

    return factory.getSuite(List.of(instance));
  }

  @TestFactory
  List<DynamicNode> runGroupWithJunit5() {
    JUnit5TestSuiteFactory factory = new JUnit5TestSuiteFactory();
    return factory.getSuite(
        List.of(
            new InnerTestExecutor("first"),
            new InnerTestGroup("group", List.of(
                new InnerTestExecutor("second"),
                new InnerTestExecutor("third"),
                new InnerTestGroup("sub group", List.of(
                    new InnerTestExecutor("forth"),
                    new InnerTestExecutor("fifth")))))));
  }

  static class InnerTestExecutor implements TestSuiteFactory.TestExecutor {
    private final String name;

    InnerTestExecutor(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void execute() throws Throwable {
      LOGGER.info("Executing {}", name);
    }
  }

  static class InnerTestGroup implements TestSuiteFactory.TestGroup {
    private final String name;

    private final List<TestSuiteFactory.Test> tests;

    InnerTestGroup(String name, List<TestSuiteFactory.Test> tests) {
      this.name = name;
      this.tests = tests;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public List<TestSuiteFactory.Test> getTests() {
      return tests;
    }
  }
}
