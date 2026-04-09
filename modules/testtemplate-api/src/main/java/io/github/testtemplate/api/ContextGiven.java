package io.github.testtemplate.api;

import org.jspecify.annotations.Nullable;

public interface ContextGiven extends Context {

  ValueStep given(String variable);

  interface ValueStep {

    @Nullable <V> V is(V value);

  }
}
