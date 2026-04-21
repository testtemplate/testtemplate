package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.TestException;

public class TestRunnerException extends TestException {

  public TestRunnerException(String message) {
    super(message);
  }

  public TestRunnerException(String message, Throwable cause) {
    super(message, cause);
  }
}
