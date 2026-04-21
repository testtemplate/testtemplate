package io.github.testtemplate.core.logger;

import io.github.testtemplate.api.Test;
import io.github.testtemplate.api.TestType;
import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.VariableType;
import io.github.testtemplate.api.logger.TestLogSectionProvider;
import io.github.testtemplate.api.logger.TestLogSnapshot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class TestLoggerTest {

  @Mock
  private Test test;

  @Nested
  class TemplateSectionTest {

    @org.junit.jupiter.api.Test
    void shouldIncludeTemplateLineInReportWhenResultIsAValue() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 42));
      logger.setResult("some result");

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("Template:\n");
      Assertions.assertThat(report).contains("  MyTest.java:42\n");
    }

    @org.junit.jupiter.api.Test
    void shouldIncludeTemplateLineInReportWhenExceptionIsThrown() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 55));
      logger.setException(new RuntimeException("boom"));

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("Template:\n");
      Assertions.assertThat(report).contains("  MyTest.java:55\n");
      Assertions.assertThat(report).contains("Exception Thrown:\n");
    }

    @org.junit.jupiter.api.Test
    void shouldFormatTemplateSourceAsFileNameColonLineNumber() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("com.example.MyTestClass", "run", "MyTestClass.java", 99));
      logger.setResult("value");

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("  MyTestClass.java:99\n");
      Assertions.assertThat(report).doesNotContain("com.example");
    }

    @org.junit.jupiter.api.Test
    void shouldSeparateTemplateSectionFromResultWithBlankLine() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 10));
      logger.setResult("value");

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("  MyTest.java:10\n\nResult:\n");
    }

    @org.junit.jupiter.api.Test
    void shouldSeparateTemplateSectionFromExceptionThrownWithBlankLine() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 10));
      logger.setException(new RuntimeException("fail"));

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("  MyTest.java:10\n\nException Thrown:\n");
    }

    @org.junit.jupiter.api.Test
    void shouldIncludeTemplateSectionEvenWhenNoVariablesLoaded() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 7));
      logger.setResult(null);

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("Template:\n");
      Assertions.assertThat(report).doesNotContain("Variables:\n");
    }

    @org.junit.jupiter.api.Test
    void shouldPlaceTemplateSectionAfterVariablesSection() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 30));

      var variable = Mockito.mock(Variable.class);
      Mockito.doReturn("myVar").when(variable).getName();
      Mockito.doReturn(VariableType.ORIGINAL).when(variable).getType();
      Mockito.doReturn("hello").when(variable).getDescription();
      logger.setLoadedVariable(variable);
      logger.setResult("result");

      var report = logger.buildReport();

      int variablesIndex = report.indexOf("Variables:\n");
      int templateIndex = report.indexOf("Template:\n");
      int resultIndex = report.indexOf("Result:\n");
      Assertions.assertThat(variablesIndex).isLessThan(templateIndex);
      Assertions.assertThat(templateIndex).isLessThan(resultIndex);
    }
  }

  @Nested
  class VariablesSectionTest {

    @org.junit.jupiter.api.Test
    void shouldOmitVariablesSectionWhenNoVariablesLoaded() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));
      logger.setResult("value");

      var report = logger.buildReport();

      Assertions.assertThat(report).doesNotContain("Variables:\n");
    }

    @org.junit.jupiter.api.Test
    void shouldIncludeVariablesSectionWhenVariablesLoaded() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));

      var variable = Mockito.mock(Variable.class);
      Mockito.doReturn("firstName").when(variable).getName();
      Mockito.doReturn(VariableType.ORIGINAL).when(variable).getType();
      Mockito.doReturn("Alice").when(variable).getDescription();
      logger.setLoadedVariable(variable);
      logger.setResult("value");

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("Variables:\n");
      Assertions.assertThat(report).contains("firstName");
      Assertions.assertThat(report).contains("Alice");
    }

    @org.junit.jupiter.api.Test
    void shouldMarkModifiedVariableWithMMarker() {
      Mockito.doReturn(TestType.ALTERNATIVE).when(test).getType();
      Mockito.doReturn("alt test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));

      var variable = Mockito.mock(Variable.class);
      Mockito.doReturn("lastName").when(variable).getName();
      Mockito.doReturn(VariableType.MODIFIED).when(variable).getType();
      Mockito.doReturn("<null>").when(variable).getDescription();
      logger.setLoadedVariable(variable);
      logger.setResult("value");

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("(M)");
    }
  }

  @Nested
  class ResultSectionTest {

    @org.junit.jupiter.api.Test
    void shouldPrintResultWhenTemplateReturnsValue() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));
      logger.setResult("Brown, Alice J");

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("Result:\n");
      Assertions.assertThat(report).contains("  Brown, Alice J\n");
      Assertions.assertThat(report).doesNotContain("Exception Thrown:\n");
    }

    @org.junit.jupiter.api.Test
    void shouldPrintExceptionThrownWhenTemplateThrows() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));
      logger.setException(new IllegalArgumentException("bad input"));

      var report = logger.buildReport();

      Assertions.assertThat(report).contains("Exception Thrown:\n");
      Assertions.assertThat(report).contains("IllegalArgumentException");
      Assertions.assertThat(report).doesNotContain("Result:\n");
    }
  }

  @Nested
  class TemplateOrderingTest {

    @org.junit.jupiter.api.Test
    void shouldRenderSectionsInTemplateOrder() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 10));

      var variable = Mockito.mock(Variable.class);
      Mockito.doReturn("x").when(variable).getName();
      Mockito.doReturn(VariableType.ORIGINAL).when(variable).getType();
      Mockito.doReturn("42").when(variable).getDescription();
      logger.setLoadedVariable(variable);
      logger.setResult("result");

      Map<String, TestLogSectionProvider> registry = new LinkedHashMap<>(TestLogger.REGISTRY);
      var report = logger.buildReport(List.of("RESULT", "VARIABLES", "TEMPLATE_SOURCE"), registry);

      int resultIndex = report.indexOf("Result:\n");
      int variablesIndex = report.indexOf("Variables:\n");
      int templateIndex = report.indexOf("Template:\n");
      Assertions.assertThat(resultIndex).isLessThan(variablesIndex);
      Assertions.assertThat(variablesIndex).isLessThan(templateIndex);
    }

    @org.junit.jupiter.api.Test
    void shouldInsertBlankLineForEmptyTemplateLine() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));
      logger.setResult("ok");

      Map<String, TestLogSectionProvider> registry = new LinkedHashMap<>(TestLogger.REGISTRY);
      var report = logger.buildReport(List.of("TEMPLATE_SOURCE", "", "RESULT"), registry);

      int templateIndex = report.indexOf("Template:\n");
      int resultIndex = report.indexOf("Result:\n");
      String between = report.substring(templateIndex, resultIndex);
      // between should contain the template source line, its trailing newline,
      // a blank line from the section separator, plus the extra blank line from the empty entry
      Assertions.assertThat(between).contains("\n\n\n");
    }

    @org.junit.jupiter.api.Test
    void shouldIgnoreUnknownSectionNameSilently() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));
      logger.setResult("ok");

      Map<String, TestLogSectionProvider> registry = new LinkedHashMap<>(TestLogger.REGISTRY);
      var report = logger.buildReport(List.of("TEMPLATE_SOURCE", "UNKNOWN_SECTION", "RESULT"), registry);

      Assertions.assertThat(report).contains("Template:\n");
      Assertions.assertThat(report).contains("Result:\n");
      Assertions.assertThat(report).doesNotContain("UNKNOWN_SECTION");
    }

    @org.junit.jupiter.api.Test
    void shouldOmitSectionWhenProviderReturnsEmptyLines() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));
      logger.setResult("ok");
      // No variables loaded — VariablesSectionProvider returns empty list

      Map<String, TestLogSectionProvider> registry = new LinkedHashMap<>(TestLogger.REGISTRY);
      var report = logger.buildReport(List.of("VARIABLES", "TEMPLATE_SOURCE", "RESULT"), registry);

      Assertions.assertThat(report).doesNotContain("Variables:\n");
      Assertions.assertThat(report).contains("Template:\n");
      Assertions.assertThat(report).contains("Result:\n");
    }

    @org.junit.jupiter.api.Test
    void shouldIncludeCustomProviderWhenRegistered() {
      Mockito.doReturn(TestType.DEFAULT).when(test).getType();
      Mockito.doReturn("my test").when(test).getName();
      var logger = new TestLogger(test);
      logger.setTemplateSource(new StackTraceElement("MyTest", "myMethod", "MyTest.java", 1));
      logger.setResult("ok");

      TestLogSectionProvider custom = new TestLogSectionProvider() {
        @Override
        public String name() {
          return "CUSTOM";
        }

        @Override
        public List<String> lines(TestLogSnapshot snapshot) {
          return List.of("Custom Section:", "  custom content");
        }
      };
      Map<String, TestLogSectionProvider> registry = new LinkedHashMap<>(TestLogger.REGISTRY);
      registry.put(custom.name(), custom);

      var report = logger.buildReport(List.of("TEMPLATE_SOURCE", "CUSTOM", "RESULT"), registry);

      Assertions.assertThat(report).contains("Custom Section:\n");
      Assertions.assertThat(report).contains("  custom content\n");
    }
  }
}
