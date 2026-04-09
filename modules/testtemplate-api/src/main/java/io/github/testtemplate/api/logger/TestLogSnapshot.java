package io.github.testtemplate.api.logger;

import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;

public interface TestLogSnapshot {

  Test test();

  Map<String, ? extends Variable> variables();

  @Nullable StackTraceElement templateSource();

  Optional<Object> result();

  Optional<Throwable> exception();

}
