package io.github.testtemplate.api.config;

import java.util.Collections;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public final class TestConfiguration implements Configuration {

  public static final TestConfiguration EMPTY = new TestConfiguration(Collections.emptyMap());

  private final Map<String, Object> properties;

  TestConfiguration(Map<String, Object> properties) {
    this.properties = Map.copyOf(properties);
  }

  @Override
  @SuppressWarnings("unchecked")
  public @Nullable <T> T get(String key) {
    return (T) properties.get(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getOrDefault(final String key, final T defaultValue)
  {
    return (T) properties.getOrDefault(key, defaultValue);
  }
}
