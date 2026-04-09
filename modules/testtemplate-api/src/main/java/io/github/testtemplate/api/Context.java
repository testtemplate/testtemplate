package io.github.testtemplate.api;

import org.jspecify.annotations.Nullable;

public interface Context {

  @Nullable <V> V get(String variable);

}
