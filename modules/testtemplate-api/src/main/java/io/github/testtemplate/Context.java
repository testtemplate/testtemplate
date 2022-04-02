package io.github.testtemplate;

public interface Context extends ContextView {

  VariableBuilder given(String variable);

  interface VariableBuilder {

    <V> V is(V value);

  }
}
