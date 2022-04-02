package io.github.testtemplate.core.listener;

import io.github.testtemplate.TestListener;
import io.github.testtemplate.TestType;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.util.HashMap;
import java.util.Map;

class DisabledTestListenerTest {

  @Test
  void shouldThrowExceptionWhenAttributeIsSetToTrue() {
    var test = new TestTest();
    test.setAttribute("ca.guig.testtemplate.test.disabled", true);
    test.setAttribute("ca.guig.testtemplate.test.disabled-reason", "this is a test");

    var listener = new DisabledTestListener();

    Assertions
        .assertThatThrownBy(() -> listener.before(test))
        .isInstanceOf(TestAbortedException.class)
        .hasMessage("The test is disabled: this is a test");
  }

  @Test
  void shouldThrowExceptionWithNullReasonWhenReasonIsUndefined() {
    var test = new TestTest();
    test.setAttribute("ca.guig.testtemplate.test.disabled", true);

    var listener = new DisabledTestListener();

    Assertions
        .assertThatThrownBy(() -> listener.before(test))
        .isInstanceOf(TestAbortedException.class)
        .hasMessage("The test is disabled: no reason");
  }

  @Test
  void shouldDoNothingWhenAttributeIsSetToFalse() {
    var test = new TestTest();
    test.setAttribute("ca.guig.testtemplate.test.disabled", false);

    var listener = new DisabledTestListener();

    listener.before(test);
  }

  @Test
  void shouldDoNothingWhenAttributeIsNotSet() {
    var test = new TestTest();

    var listener = new DisabledTestListener();

    listener.before(test);
  }

  private static class TestTest implements TestListener.Test {

    private final Map<String, Object> attributes = new HashMap<>();

    @Override
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public TestType getType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getVariableNames() {
      throw new UnsupportedOperationException();
    }

    @Override
    public TestListener.Variable getVariable(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String key) {
      return attributes.get(key);
    }

    @Override
    public Object getAttribute(String key, Object defaultValue) {
      return attributes.getOrDefault(key, defaultValue);
    }

    @Override
    public void setAttribute(String key, Object value) {
      attributes.put(key, value);
    }

    @Override
    public void clearAttribute(String key) {
      attributes.remove(key);
    }
  }
}
