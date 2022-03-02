package com.github.testtemplate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TestTemplateTest {

  @Test
  void defaultTestShouldThrowException() {
    Assertions
        .assertThatThrownBy(() -> TestTemplate.defaultTest("test"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
