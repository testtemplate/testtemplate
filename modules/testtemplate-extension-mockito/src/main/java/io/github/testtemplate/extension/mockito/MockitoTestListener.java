package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.listener.TestListener;

import org.mockito.Mockito;
import org.slf4j.Logger;

import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;

public class MockitoTestListener implements TestListener {

  private static final Logger LOGGER = getLogger(MockitoTestListener.class);

  @Override
  public void before(Test test) {
    test.getVariableNames().forEach(name -> {
      var variable = test.getVariable(name);
      if (TRUE.equals(variable.getMetadata(MockitoMetadata.Variable.IS_MOCK, false))) {
        LOGGER.info("Loading mock: {}", name);
        variable.getValue();
      }
    });
  }

  @Override
  public void after(Test test) {
    test.getVariableNames().forEach(name -> {
      var variable = test.getVariable(name);
      if (TRUE.equals(variable.getMetadata(MockitoMetadata.Variable.IS_MOCK, false))) {
        LOGGER.info("Resetting mock: {}", name);
        Mockito.reset(variable.getValue());
      }
    });
  }
}
