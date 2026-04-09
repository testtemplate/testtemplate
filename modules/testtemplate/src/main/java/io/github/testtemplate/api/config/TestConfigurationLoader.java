package io.github.testtemplate.api.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.Map;

public final class TestConfigurationLoader {

  private TestConfigurationLoader() {}

  public static TestConfiguration load() {
    return load(Thread.currentThread().getContextClassLoader());
  }

  public static TestConfiguration load(ClassLoader classLoader) {
    try (InputStream stream = classLoader.getResourceAsStream("testtemplate.yaml")) {
      if (stream == null) {
        return TestConfiguration.EMPTY;
      }

      Map<String, Object> properties = new Yaml().load(stream);

      if (properties == null) {
        return TestConfiguration.EMPTY;
      }

      return new TestConfiguration(properties);
    } catch (YAMLException e) {
      throw new TestConfigurationException("Failed to parse testtemplate.yaml: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new TestConfigurationException("Failed to read testtemplate.yaml: " + e.getMessage(), e);
    }
  }
}
