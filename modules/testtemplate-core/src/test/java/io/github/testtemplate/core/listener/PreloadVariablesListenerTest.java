package io.github.testtemplate.core.listener;

import io.github.testtemplate.TestListener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class PreloadVariablesListenerTest {

  @Mock
  private TestListener.Test test;

  @Test
  void shouldCallGetValueOnVariableWhenMetadataIsSetToTrue() {
    Mockito.doReturn(Set.of("var-a", "var-b", "var-c")).when(test).getVariableNames();
    var varA = Mockito.spy(new TestVariable("ca.guig.testtemplate.variable.preload", true));
    Mockito.doReturn(varA).when(test).getVariable("var-a");
    var varB = Mockito.spy(new TestVariable("ca.guig.testtemplate.variable.preload", false));
    Mockito.doReturn(varB).when(test).getVariable("var-b");
    var varC = Mockito.spy(new TestVariable());
    Mockito.doReturn(varC).when(test).getVariable("var-c");

    var listener = new PreloadVariablesListener();

    listener.before(test);

    Mockito.verify(varA).getValue();
    Mockito.verify(varB, Mockito.never()).getValue();
    Mockito.verify(varC, Mockito.never()).getValue();
  }

  private static class TestVariable implements TestListener.Variable {

    private final Map<String, Object> metadata = new HashMap<>();

    TestVariable() {
    }

    TestVariable(String key, Object value) {
      metadata.put(key, value);
    }

    @Override
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public TestListener.VariableType getType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue() {
      return "ok";
    }

    @Override
    public Object getMetadata(String key) {
      return metadata.get(key);
    }

    @Override
    public Object getMetadata(String key, Object defaultValue) {
      return metadata.getOrDefault(key, defaultValue);
    }
  }
}
