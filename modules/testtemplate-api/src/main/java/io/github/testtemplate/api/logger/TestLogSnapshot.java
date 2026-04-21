package io.github.testtemplate.api.logger;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface TestLogSnapshot {

  Test test();

  Map<String, ? extends Variable> variables();

  @Nullable StackTraceElement templateSource();

  Optional<Object> result();

  Optional<Throwable> exception();

}
