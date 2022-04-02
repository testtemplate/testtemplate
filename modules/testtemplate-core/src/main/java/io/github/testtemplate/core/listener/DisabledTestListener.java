package io.github.testtemplate.core.listener;

import io.github.testtemplate.TestListener;

import org.opentest4j.TestAbortedException;

import static java.lang.Boolean.TRUE;

public final class DisabledTestListener implements TestListener {

  public static final String ATTRIBUTE_TEST_DISABLED = "ca.guig.testtemplate.test.disabled";
  public static final String ATTRIBUTE_TEST_DISABLED_REASON = "ca.guig.testtemplate.test.disabled-reason";

  @Override
  public void before(Test test) {
    if (TRUE.equals(test.getAttribute(ATTRIBUTE_TEST_DISABLED, false))) {
      String reason = (String) test.getAttribute(ATTRIBUTE_TEST_DISABLED_REASON, "no reason");
      throw new TestAbortedException("The test is disabled: " + reason);
    }
  }
}
