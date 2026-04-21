package io.github.testtemplate.api.listener;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;

import org.jspecify.annotations.Nullable;

public interface TestListener {

  default void before(Test test) {}

  default void after(Test test) {}

  default void result(Test test, @Nullable Object result) {}

  default void exception(Test test, Throwable exception) {}

  default void variable(Test test, Variable variable) {}

}
