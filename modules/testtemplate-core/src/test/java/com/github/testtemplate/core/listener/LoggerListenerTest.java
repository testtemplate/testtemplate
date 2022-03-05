package com.github.testtemplate.core.listener;

import com.github.testtemplate.TestType;

import org.junit.jupiter.api.Test;

import static com.github.testtemplate.TestListener.VariableType.MODIFIED;
import static com.github.testtemplate.TestListener.VariableType.ORIGINAL;
import static org.assertj.core.api.Assertions.assertThat;

class LoggerListenerTest {

  @Test
  void reportShouldShowTestTypeAndName() {
    LoggerListener.LogContext context = new LoggerListener.LogContext("test-name", TestType.DEFAULT);
    var report = LoggerListener.buildReport(context);
    assertThat(report).contains("(DEFAULT) test-name");
  }

  @Test
  void reportShouldNotShowVariableSectionWhenThereIsNoVariables() {
    LoggerListener.LogContext context = new LoggerListener.LogContext("test-name", TestType.DEFAULT);
    var report = LoggerListener.buildReport(context);
    assertThat(report).doesNotContain("Variables:");
  }

  @Test
  void reportShouldShowVariableInColumn() {
    LoggerListener.LogContext context = new LoggerListener.LogContext("test-name", TestType.DEFAULT);
    context.addVariable("short-name", ORIGINAL, "some value");
    context.addVariable("very-very-very-very-long-name", ORIGINAL, "some value");
    context.addVariable("another-long-name", ORIGINAL, "some value");

    var report = LoggerListener.buildReport(context);

    assertThat(report).contains("Variables:\n"
        + "  short-name                        = some value\n"
        + "  very-very-very-very-long-name     = some value\n"
        + "  another-long-name                 = some value\n");
  }

  @Test
  void reportShouldShowModifiedVariable() {
    LoggerListener.LogContext context = new LoggerListener.LogContext("test-name", TestType.DEFAULT);
    context.addVariable("original-value", ORIGINAL, "some value");
    context.addVariable("modified-value", MODIFIED, "some value");

    var report = LoggerListener.buildReport(context);

    assertThat(report).contains("Variables:\n"
        + "  original-value     = some value\n"
        + "  modified-value (M) = some value\n");
  }

  @Test
  void reportShouldShowResult() {
    LoggerListener.LogContext context = new LoggerListener.LogContext("test-name", TestType.DEFAULT);
    context.setResult("some result");

    var report = LoggerListener.buildReport(context);

    System.out.println(report);

    assertThat(report).contains("Result:\n  some result\n");
  }

  @Test
  void reportShouldShowExceptionWithStackTrace() {
    try {
      throw new IllegalArgumentException("test");
    } catch (Exception e) {
      LoggerListener.LogContext context = new LoggerListener.LogContext("test-name", TestType.DEFAULT);
      context.setException(e);

      var report = LoggerListener.buildReport(context);

      System.out.println(report);

      assertThat(report).contains("Exception Thrown:\n  java.lang.IllegalArgumentException: test");
    }
  }
}
