package io.github.testtemplate.api;

import org.jspecify.annotations.Nullable;

public interface ValueDescriptor {

  boolean isSupported(@Nullable Object value);

  String toString(@Nullable Object value);

}
