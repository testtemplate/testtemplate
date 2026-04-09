package io.github.testtemplate.extension.mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.builder.DefaultBuilder;
import io.github.testtemplate.api.function.ExceptionalFunction;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MockitoExtensionTest {

  private static final String VARIABLE = "test-mock";

  @Nested
  class UseDefaultTestTemplateGivenBuilder {

    @Mock
    private DefaultBuilder.GivenStep.MetadataStep<Object> builder;

    @Mock
    private ContextGiven context;

    @Captor
    private ArgumentCaptor<ExceptionalFunction<ContextGiven, ?>> functionIsCaptor;

    @BeforeEach
    void setUp() {
      var next = Mockito.mock(DefaultBuilder.GivenStep.class);
      Mockito.doReturn(builder).when(builder).metadata(Mockito.any(), Mockito.any());
      Mockito.doReturn(next).when(builder).is(Mockito.<ExceptionalFunction<ContextGiven, ?>>any());
    }

    @Test
    void shouldReturnMockWithStubbings() throws Exception {
      new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .mock(TestService.class)
            .invoking((m, c) -> m.read("1234"))
              .willReturn(c -> new TestEntity("1234", "test-title", "test-content", 10L))
            .invoking(m -> m.create(Mockito.any()))
              .willReturn(() -> new TestEntity("1234", "test-title", "test-content", 10L))
          .when(ctx -> null);

      verify(builder).is(functionIsCaptor.capture());

      var result = functionIsCaptor.getValue().apply(context);
      var details = Mockito.mockingDetails(result);
      assertThat(details.isMock()).isTrue();
      assertThat(details.getStubbings()).hasSize(2);
    }
  }

  private interface TestService {

    TestEntity create(TestEntity entity);

    TestEntity read(String id);

  }

  private static final class TestEntity {

    private final String id;
    private final String title;
    private final String content;
    private final Long rank;

    private TestEntity(String id, String title, String content, Long rank) {
      this.id = id;
      this.title = title;
      this.content = content;
      this.rank = rank;
    }
  }
}
