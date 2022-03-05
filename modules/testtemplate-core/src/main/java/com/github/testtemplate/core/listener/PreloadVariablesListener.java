package com.github.testtemplate.core.listener;

import com.github.testtemplate.TestListener;

import static java.lang.Boolean.TRUE;

public class PreloadVariablesListener implements TestListener {

  public static final String METADATA_PRELOAD_VARIABLE = "ca.guig.testtemplate.variable.preload";

  @Override
  public void before(Test test) {
    test.getVariableNames().forEach(name -> {
      var variable = test.getVariable(name);
      if (TRUE.equals(variable.getMetadata(METADATA_PRELOAD_VARIABLE, false))) {
        variable.getValue();
      }
    });
  }
}
