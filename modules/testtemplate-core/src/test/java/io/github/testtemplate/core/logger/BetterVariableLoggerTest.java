package io.github.testtemplate.core.logger;

import io.github.testtemplate.Variable;
import io.github.testtemplate.VariableType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BetterVariableLoggerTest {

  @Test
  void shouldHandleNullObject() {
    var variable = new TestVariable(null);
    assertThat(BetterVariableLogger.toValueString(variable)).isEqualTo("<null>");
  }

  @Test
  void shouldHandleObject() {
    var value = new Object() {
      @Override
      public String toString() {
        return "TEST OBJECT";
      }
    };
    var variable = new TestVariable(value);
    assertThat(BetterVariableLogger.toValueString(variable)).isEqualTo("TEST OBJECT");
  }

  @Test
  void shouldHandleEmptyString() {
    var variable = new TestVariable("");
    assertThat(BetterVariableLogger.toValueString(variable)).isEqualTo("<empty>");
  }

  @Test
  void shouldHandleBlankString() {
    var variable = new TestVariable("  ");
    assertThat(BetterVariableLogger.toValueString(variable)).isEqualTo("<blank>");
  }

  @Test
  void shouldHandleString() {
    var variable = new TestVariable("TEST STRING");
    assertThat(BetterVariableLogger.toValueString(variable)).isEqualTo("TEST STRING");
  }

  private static final class TestVariable implements Variable {
    private final Object value;

    TestVariable(Object value) {
      this.value = value;
    }

    @Override
    public String getName() {
      return "not used";
    }

    @Override
    public VariableType getType() {
      return VariableType.MODIFIED;
    }

    @Override
    public Object getValue() {
      return value;
    }

    @Override
    public Object getMetadata(String key) {
      return null;
    }

    @Override
    public Object getMetadata(String key, Object defaultValue) {
      return defaultValue;
    }
  }
}
