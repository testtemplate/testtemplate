package io.github.testtemplate;

public interface ContextView {

  <V> V get(String variable);

}
