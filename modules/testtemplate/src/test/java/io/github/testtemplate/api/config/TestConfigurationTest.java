package io.github.testtemplate.api.config;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TestConfigurationTest {

  @Test
  void shouldReturnValueForKnownKey() {
    TestConfiguration config = new TestConfiguration(Map.of("key", "value"));

    Assertions.assertThat(config.get("key")).isEqualTo("value");
  }

  @Test
  void shouldReturnNullForUnknownKey() {
    TestConfiguration config = new TestConfiguration(Map.of("key", "value"));

    Assertions.assertThat(config.get("missing")).isNull();
  }

  @Test
  void shouldReportIsEmptyWhenConstructedWithEmptyMap() {
    TestConfiguration config = new TestConfiguration(Map.of());

    Assertions.assertThat(config.isEmpty()).isTrue();
  }

  @Test
  void shouldReportIsNotEmptyWhenConstructedWithNonEmptyMap() {
    TestConfiguration config = new TestConfiguration(Map.of("key", "value"));

    Assertions.assertThat(config.isEmpty()).isFalse();
  }

  @Test
  void shouldBeImmutable() {
    Map<String, Object> mutableMap = new HashMap<>();
    mutableMap.put("key", "value");
    TestConfiguration config = new TestConfiguration(mutableMap);

    // Verify original map mutation does not affect configuration
    mutableMap.put("key", "changed");

    Assertions.assertThat(config.get("key")).isEqualTo("value");
  }
}
