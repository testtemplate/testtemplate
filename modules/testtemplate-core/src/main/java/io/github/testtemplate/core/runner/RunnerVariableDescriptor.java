package io.github.testtemplate.core.runner;

import io.github.testtemplate.api.ValueDescriptor;
import io.github.testtemplate.core.runner.descriptor.ObjectDescriptor;
import io.github.testtemplate.core.runner.descriptor.StringDescriptor;

import org.jspecify.annotations.Nullable;

import java.util.List;

final class RunnerVariableDescriptor {

  // TODO Add support for custom descriptors
  private static final List<ValueDescriptor> DESCRIPTORS = List.of(
      new StringDescriptor(),
      new ObjectDescriptor());

  public String describe(@Nullable Object value) {
    return DESCRIPTORS.stream()
        .filter(l -> l.isSupported(value))
        .findFirst()
        .map(l -> l.toString(value))
        .orElseThrow();
  }
}
