package io.github.testtemplate.core;

import io.github.testtemplate.TestType;
import io.github.testtemplate.core.runner.TestRunnerFactory;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestInstanceTest {

  @Test
  void testOfExecutor() {
    var test = new TestDefinition<Object>(
        "single",
        TestType.ALTERNATIVE,
        c -> 1 + 2,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        r -> {},
        Map.of());

    var runnerFactory = new TestRunnerFactory(List.of());

    var instance = TestInstance.of(test, runnerFactory);

    assertThat(instance)
        .isInstanceOf(TestInstance.TestExecutorInstance.class)
        .hasFieldOrPropertyWithValue("name", "single");
  }

  @Test
  void testOfGroup() {
    var test = new TestDefinition<Object>(
        "group",
        TestType.ALTERNATIVE,
        c -> 1 + 2,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.singletonList(new TestParameter("var", List.of(c -> "1", c -> "2"))),
        r -> {},
        Map.of());

    var runnerFactory = new TestRunnerFactory(List.of());

    var instance = TestInstance.of(test, runnerFactory);

    assertThat(instance)
        .isInstanceOf(TestInstance.TestGroupInstance.class)
        .hasFieldOrPropertyWithValue("name", "group");

    assertThat(((TestInstance.TestGroupInstance<?>) instance).getTests()).size().isEqualTo(2);
  }
}
