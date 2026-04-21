package io.github.testtemplate.api.config;

import org.jspecify.annotations.Nullable;

public interface Configuration {

  @Nullable <T> T get(String key);

  <T> T getOrDefault(String key, T defaultValue);

}
