package io.github.testtemplate.core.logger;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.logger.TestLogSectionProvider;
import io.github.testtemplate.api.logger.TestLogSnapshot;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import static org.slf4j.LoggerFactory.getLogger;

public class TestLogger implements TestLogSnapshot {

  private static final Logger LOGGER = getLogger(TestLogger.class);

  private static final String DEFAULT_TEMPLATE = "VARIABLES\\nTEMPLATE_SOURCE\\nRESULT";

  static final List<String> TEMPLATE;

  static final Map<String, TestLogSectionProvider> REGISTRY;

  static {
    TEMPLATE = loadTemplate();
    REGISTRY = buildRegistry();
  }

  private final Test test;

  private final Map<String, Variable> loadedVariables = new LinkedHashMap<>();

  @Nullable
  private StackTraceElement templateSource;

  @Nullable
  private Object result;

  @Nullable
  private Throwable exception;

  private boolean exceptionThrown;

  public TestLogger(Test test) {
    this.test = test;
  }

  // --- TestLogSnapshot implementation ---

  @Override
  public Test test() {
    return test;
  }

  @Override
  public Map<String, Variable> variables() {
    return Collections.unmodifiableMap(loadedVariables);
  }

  @Override
  @Nullable
  public StackTraceElement templateSource() {
    return templateSource;
  }

  @Override
  public Optional<Object> result() {
    if (exceptionThrown) {
      return Optional.empty();
    }
    return Optional.ofNullable(result);
  }

  @Override
  public Optional<Throwable> exception() {
    if (exceptionThrown) {
      return Optional.ofNullable(exception);
    }
    return Optional.empty();
  }

  // --- Mutation methods ---

  public void setTemplateSource(StackTraceElement source) {
    this.templateSource = source;
  }

  public void setLoadedVariable(Variable variable) {
    loadedVariables.put(variable.getName(), variable);
  }

  public void setResult(Object result) {
    this.result = result;
    this.exceptionThrown = false;
  }

  public void setException(Throwable exception) {
    this.exception = exception;
    this.exceptionThrown = true;
  }

  public void logReport() {
    LOGGER.info("\n{}", buildReport());
  }

  String buildReport() {
    return buildReport(TEMPLATE, REGISTRY);
  }

  String buildReport(List<String> template, Map<String, TestLogSectionProvider> registry) {
    StringBuilder sb = new StringBuilder();

    sb.append("================================================================================\n");
    sb.append("(").append(test.getType()).append(") ").append(test.getName()).append("\n");
    sb.append("\n");

    for (String name : template) {
      if (name.isEmpty()) {
        sb.append("\n");
      } else {
        TestLogSectionProvider provider = registry.get(name);
        if (provider != null) {
          List<String> lines = provider.lines(this);
          if (!lines.isEmpty()) {
            for (String line : lines) {
              sb.append(line).append("\n");
            }
            sb.append("\n");
          }
        }
      }
    }

    sb.append("================================================================================\n");

    return sb.toString();
  }

  // --- Static initialization helpers ---

  private static List<String> loadTemplate() {
    String raw = DEFAULT_TEMPLATE;
    try {
      InputStream is = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("ntt.properties");
      if (is != null) {
        Properties props = new Properties();
        try (is) {
          props.load(is);
        }
        String value = props.getProperty("ntt.log.template");
        if (value != null) {
          raw = value;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("Failed to load ntt.properties, using default log template", e);
    }
    return Arrays.asList(raw.split("\\\\n", -1));
  }

  private static Map<String, TestLogSectionProvider> buildRegistry() {
    Map<String, TestLogSectionProvider> registry = new LinkedHashMap<>();

    // Built-in providers — always registered and cannot be overridden
    for (TestLogSectionProvider builtin : List.of(
        new VariablesSectionProvider(),
        new TemplateSectionProvider(),
        new ResultSectionProvider()
    )) {
      registry.put(builtin.name(), builtin);
    }

    // Extension providers via ServiceLoader
    for (TestLogSectionProvider provider : ServiceLoader.load(TestLogSectionProvider.class)) {
      if (registry.containsKey(provider.name())) {
        LOGGER.warn("TestLogSectionProvider '{}' conflicts with a built-in section and will be ignored.",
            provider.name());
      } else {
        registry.put(provider.name(), provider);
      }
    }

    return Collections.unmodifiableMap(registry);
  }

}
