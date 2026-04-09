package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.api.Variable;
import io.github.testtemplate.api.logger.TestLogSnapshot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class MockitoLogSectionProviderTest {

  private final MockitoLogSectionProvider provider = new MockitoLogSectionProvider();

  @Mock
  private TestLogSnapshot snapshot;

  @Test
  void shouldReturnNameMockitoStubs() {
    Assertions.assertThat(provider.name()).isEqualTo("MOCKITO_STUBS");
  }

  @Nested
  class LinesTest {

    @Test
    void shouldReturnEmptyLinesWhenNoMockVariables() {
      var variable = Mockito.mock(Variable.class);
      Mockito.doReturn(false).when(variable).getMetadata(MockitoMetadata.Variable.IS_MOCK);
      Mockito.doReturn(Map.of("service", variable)).when(snapshot).variables();

      var lines = provider.lines(snapshot);

      Assertions.assertThat(lines).isEmpty();
    }

    @Test
    void shouldReturnEmptyLinesWhenMocksHaveNoStubbings() {
      var mockObject = Mockito.mock(TestService.class);
      var variable = Mockito.mock(Variable.class);
      Mockito.doReturn(true).when(variable).getMetadata(MockitoMetadata.Variable.IS_MOCK);
      Mockito.doReturn(mockObject).when(variable).getValue();
      Mockito.doReturn(Map.of("service", variable)).when(snapshot).variables();

      var lines = provider.lines(snapshot);

      Assertions.assertThat(lines).isEmpty();
    }

    @Test
    void shouldFormatStubbingsForEachMockVariable() {
      var mockObject = Mockito.mock(TestService.class);
      Mockito.lenient().doReturn("stubbed-value").when(mockObject).getValue();

      var variable = Mockito.mock(Variable.class);
      Mockito.doReturn(true).when(variable).getMetadata(MockitoMetadata.Variable.IS_MOCK);
      Mockito.doReturn(mockObject).when(variable).getValue();

      // Use LinkedHashMap to preserve insertion order for assertions
      var variables = new java.util.LinkedHashMap<String, Variable>();
      variables.put("myService", variable);
      Mockito.doReturn(variables).when(snapshot).variables();

      var lines = provider.lines(snapshot);

      Assertions.assertThat(lines).isNotEmpty();
      Assertions.assertThat(lines.get(0)).isEqualTo("Mockito Stubs:");
      Assertions.assertThat(lines.get(1)).isEqualTo("  myService:");
      Assertions.assertThat(lines.get(2)).contains("getValue");
    }
  }

  private interface TestService {

    String getValue();

  }

}
