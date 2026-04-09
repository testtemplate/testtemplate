package io.github.testtemplate.api.config;

import io.github.testtemplate.api.TestException;

class TestConfigurationException extends TestException {

  TestConfigurationException(String message) {
    super(message);
  }

  TestConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
