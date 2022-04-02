package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.AlternativeTestTemplateExceptBuilder;
import io.github.testtemplate.AlternativeTestValidatorBuilder;
import io.github.testtemplate.Context;
import io.github.testtemplate.ContextView;
import io.github.testtemplate.DefaultTestTemplateBuilder;
import io.github.testtemplate.DefaultTestTemplateGivenBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MockExtensionTest {

  private static final String VARIABLE = "test-mock";

  @Nested
  class UseDefaultTestTemplateGivenBuilder {

    @Mock
    private DefaultTestTemplateGivenBuilder<Object> builder;

    @Mock
    private Context context;

    @Captor
    private ArgumentCaptor<Function<Context, ?>> functionIsCaptor;

    @BeforeEach
    void setUp() {
      var next = Mockito.mock(DefaultTestTemplateBuilder.class);
      Mockito.doReturn(builder).when(builder).preload();
      Mockito.doReturn(next).when(builder).is(Mockito.<Function<Context, ?>>any());
    }

    @Test
    void shouldReturnMockWithStubbings() {
      new MockExtension<>()
          .getExtension(builder, VARIABLE)
          .mock(TestService.class)
          .invoking((m, c) -> m.read("1234"))
          .willReturn(c -> new TestEntity("1234", "test-title", "test-content", 10L))
          .invoking(m -> m.create(Mockito.any()))
          .willReturn(() -> new TestEntity("1234", "test-title", "test-content", 10L))
          .when(null);

      verify(builder).is(functionIsCaptor.capture());

      var result = functionIsCaptor.getValue().apply(context);
      var details = Mockito.mockingDetails(result);
      assertThat(details.isMock()).isTrue();
      assertThat(details.getStubbings()).hasSize(2);
    }
  }

  @Nested
  class UseAlternativeTestTemplateExceptBuilder {

    @Mock
    private AlternativeTestTemplateExceptBuilder<Object, Object> builder;

    @Mock
    private Context context;

    @Captor
    private ArgumentCaptor<Function<ContextView, ?>> functionIsCaptor;

    @BeforeEach
    void setUp() {
      var next = Mockito.mock(AlternativeTestValidatorBuilder.class);
      Mockito.doReturn(next).when(builder).is(Mockito.<Function<ContextView, ?>>any());
    }

    @Test
    void shouldReturnMockWithStubbings() {
      new MockExtension<>()
          .getExtension(builder, VARIABLE)
          .invoking((TestService m, ContextView c) -> m.read("1234"))
          .willReturn(c -> new TestEntity("1234", "test-title", "test-content", 10L))
          .except(null);

      verify(builder).is(functionIsCaptor.capture());

      Mockito.doReturn(Mockito.mock(TestService.class)).when(context).get(VARIABLE);
      var result = functionIsCaptor.getValue().apply(context);

      var details = Mockito.mockingDetails(result);
      assertThat(details.isMock()).isTrue();
      assertThat(details.getStubbings()).hasSize(1);
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
