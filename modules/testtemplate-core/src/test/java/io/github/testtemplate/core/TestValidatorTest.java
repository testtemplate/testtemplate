package io.github.testtemplate.core;

import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.function.ExceptionalConsumer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TestValidatorTest {

  @Test
  void shouldStoreFunction() {
    ExceptionalConsumer<ContextResult<Object>> function = ctx -> {};
    var validator = new TestValidator<>(function, null);

    Assertions.assertThat(validator.getFunction()).isSameAs(function);
  }

  @Test
  void shouldStoreSource() {
    ExceptionalConsumer<ContextResult<Object>> function = ctx -> {};
    var source = new StackTraceElement("com.example.Foo", "bar", "Foo.java", 42);
    var validator = new TestValidator<>(function, source);

    Assertions.assertThat(validator.getSource()).isSameAs(source);
  }

  @Test
  void shouldAllowNullSource() {
    ExceptionalConsumer<ContextResult<Object>> function = ctx -> {};
    var validator = new TestValidator<>(function, null);

    Assertions.assertThat(validator.getSource()).isNull();
  }
}
