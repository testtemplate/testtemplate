package io.github.testtemplate.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class TestConfigurationLoaderTest {

  @Nested
  class LoadFromClasspathTest {

    @Test
    @Disabled("Should be revisited")
    void shouldLoadYamlFileWhenPresentOnClasspath() {
      TestConfiguration config = TestConfigurationLoader.load(TestConfigurationLoaderTest.class.getClassLoader());

      Assertions.assertThat(config.<String>get("testtemplate.example")).isEqualTo("hello from test");
    }

    @Test
    void shouldReturnEmptyConfigurationWhenFileNotFoundOnClasspath() {
      ClassLoader noFileClassLoader = new ClassLoader() {
        @Override
        public InputStream getResourceAsStream(String name) {
          return null;
        }
      };

      TestConfiguration config = TestConfigurationLoader.load(noFileClassLoader);

      Assertions.assertThat(config).isEqualTo(TestConfiguration.EMPTY);
    }

    @Test
    void shouldReturnEmptyConfigurationWhenYamlFileIsEmpty() {
      ClassLoader emptyFileClassLoader = new ClassLoader() {
        @Override
        public InputStream getResourceAsStream(String name) {
          return new ByteArrayInputStream(new byte[0]);
        }
      };

      TestConfiguration config = TestConfigurationLoader.load(emptyFileClassLoader);

      Assertions.assertThat(config).isEqualTo(TestConfiguration.EMPTY);
    }
  }

  @Nested
  class MalformedYamlTest {

    @Test
    void shouldThrowTestConfigurationExceptionWhenYamlRootIsNotAMap() {
      ClassLoader listRootClassLoader = new ClassLoader() {
        @Override
        public InputStream getResourceAsStream(String name) {
          return new ByteArrayInputStream("- item\n- another".getBytes(StandardCharsets.UTF_8));
        }
      };

      Assertions.assertThatThrownBy(() -> TestConfigurationLoader.load(listRootClassLoader))
          .isInstanceOf(TestConfigurationException.class);
    }

    @Test
    void shouldWrapSnakeYamlExceptionInTestConfigurationException() {
      ClassLoader invalidYamlClassLoader = new ClassLoader() {
        @Override
        public InputStream getResourceAsStream(String name) {
          // Tabs used for indentation are invalid in YAML
          return new ByteArrayInputStream("key:\n\tvalue".getBytes(StandardCharsets.UTF_8));
        }
      };

      Assertions.assertThatThrownBy(() -> TestConfigurationLoader.load(invalidYamlClassLoader))
          .isInstanceOf(TestConfigurationException.class)
          .hasCauseInstanceOf(YAMLException.class);
    }
  }
}
