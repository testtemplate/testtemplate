package io.github.testtemplate.core.runner;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.github.testtemplate.api.ValueDescriptor;
import io.github.testtemplate.core.runner.descriptor.ObjectDescriptor;
import io.github.testtemplate.core.runner.descriptor.StringDescriptor;

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
