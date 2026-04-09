package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.api.TestException;

public class MockitoExtensionException extends TestException {

  public MockitoExtensionException(String message) {
    super(message);
  }

  public MockitoExtensionException(String message, Throwable cause) {
    super(message, cause);
  }
}
