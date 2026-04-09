package io.github.testtemplate.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class TestConfigurationTest {

  @Test
  void shouldReturnValueForKnownKey() {
    TestConfiguration config = new TestConfiguration(Map.of("key", "value"));

    Assertions.assertThat(config.<String>get("key")).isEqualTo("value");
  }

  @Test
  void shouldReturnNullForUnknownKey() {
    TestConfiguration config = new TestConfiguration(Map.of("key", "value"));

    Assertions.assertThat(config.<String>get("missing")).isNull();
  }

  @Test
  void shouldBeImmutable() {
    Map<String, Object> mutableMap = new HashMap<>();
    mutableMap.put("key", "value");
    TestConfiguration config = new TestConfiguration(mutableMap);

    // Verify original map mutation does not affect configuration
    mutableMap.put("key", "changed");

    Assertions.assertThat(config.<String>get("key")).isEqualTo("value");
  }
}
