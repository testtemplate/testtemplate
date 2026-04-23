package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.builder.AlternativeBuilder;
import io.github.testtemplate.api.builder.DefaultBuilder;
import io.github.testtemplate.api.builder.SetupBuilder;
import io.github.testtemplate.api.function.ExceptionalFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

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

    @Test
    void shouldReturnMockWithChainedResponses() throws Exception {
      var entity1 = new TestEntity("1", "title1", "content1", 1L);
      var entity2 = new TestEntity("2", "title2", "content2", 2L);
      var ex = new RuntimeException("expected");

      new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .mock(TestService.class)
            .invoking(m -> m.read("1234"))
              .willReturn(() -> entity1)
              .willAnswer(i -> entity2)
              .willThrow(ex)
          .when(ctx -> null);

      verify(builder).is(functionIsCaptor.capture());

      var mock = (TestService) functionIsCaptor.getValue().apply(context);
      assertThat(mock.read("1234")).isSameAs(entity1);
      assertThat(mock.read("1234")).isSameAs(entity2);
      assertThatThrownBy(() -> mock.read("1234")).isSameAs(ex);
    }

    @Test
    void shouldNotRegisterStubbingWhenInvokingHasNoResponseConfigured() throws Exception {
      var invokingStep = new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .mock(TestService.class);
      invokingStep.invoking(m -> m.read("1234"));  // return value ignored — simulates user mistake
      invokingStep.when(ctx -> null);

      verify(builder).is(functionIsCaptor.capture());

      var mock = functionIsCaptor.getValue().apply(context);
      assertThat(Mockito.mockingDetails(mock).getStubbings()).isEmpty();
    }
  }

  @Nested
  class UseAlternativeTestTemplateExceptBuilder {

    @Mock
    private AlternativeBuilder.ExceptStep.MetadataStep<Object, Object> builder;

    @Mock
    private Context context;

    @Captor
    private ArgumentCaptor<ExceptionalFunction<Context, ?>> functionIsCaptor;

    @BeforeEach
    void setUp() {
      var next = Mockito.mock(AlternativeBuilder.ExceptStep.PostStep.class);
      Mockito.doReturn(builder).when(builder).metadata(Mockito.any(), Mockito.any());
      Mockito.doReturn(next).when(builder).is(Mockito.<ExceptionalFunction<Context, ?>>any());
      Mockito.doReturn(Mockito.mock(TestService.class)).when(context).get(VARIABLE);
    }

    @Test
    void shouldReturnMockWithStubbings() throws Exception {
      new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .invoking((TestService m, Context c) -> m.read("1234"))
            .willReturn(c -> new TestEntity("1234", "test-title", "test-content", 10L));

      verify(builder).is(functionIsCaptor.capture());

      var result = functionIsCaptor.getValue().apply(context);
      var details = Mockito.mockingDetails(result);
      assertThat(details.isMock()).isTrue();
      assertThat(details.getStubbings()).hasSize(1);
    }

    @Test
    void shouldReturnMockWithChainedResponses() throws Exception {
      var entity1 = new TestEntity("1", "title1", "content1", 1L);
      var entity2 = new TestEntity("2", "title2", "content2", 2L);
      var ex = new RuntimeException("expected");

      new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .invoking((TestService m, Context c) -> m.read("1234"))
            .willReturn(() -> entity1)
            .willAnswer(i -> entity2)
            .willThrow(ex);

      verify(builder).is(functionIsCaptor.capture());

      var mock = (TestService) functionIsCaptor.getValue().apply(context);
      assertThat(mock.read("1234")).isSameAs(entity1);
      assertThat(mock.read("1234")).isSameAs(entity2);
      assertThatThrownBy(() -> mock.read("1234")).isSameAs(ex);
    }

    @Test
    void shouldNotRegisterStubbingWhenInvokingHasNoResponseConfigured() throws Exception {
      new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .invoking((TestService m, Context c) -> m.read("1234"));  // no will* — ResponseStep ignored

      verify(builder).is(functionIsCaptor.capture());

      var mock = functionIsCaptor.getValue().apply(context);
      assertThat(Mockito.mockingDetails(mock).getStubbings()).isEmpty();
    }
  }

  @Nested
  class UseSetupBuilderGivenBuilder {

    @Mock
    private SetupBuilder.MetadataStep builder;

    @Mock
    private ContextGiven context;

    @Captor
    private ArgumentCaptor<ExceptionalFunction<ContextGiven, ?>> functionIsCaptor;

    @BeforeEach
    void setUp() {
      var next = Mockito.mock(SetupBuilder.GivenStep.class);
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
              .willReturn(() -> new TestEntity("1234", "test-title", "test-content", 10L));

      verify(builder).is(functionIsCaptor.capture());

      var result = functionIsCaptor.getValue().apply(context);
      var details = Mockito.mockingDetails(result);
      assertThat(details.isMock()).isTrue();
      assertThat(details.getStubbings()).hasSize(2);
    }

    @Test
    void shouldReturnMockWithChainedResponses() throws Exception {
      var entity1 = new TestEntity("1", "title1", "content1", 1L);
      var entity2 = new TestEntity("2", "title2", "content2", 2L);
      var ex = new RuntimeException("expected");

      new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .mock(TestService.class)
            .invoking(m -> m.read("1234"))
              .willReturn(() -> entity1)
              .willAnswer(i -> entity2)
              .willThrow(ex);

      verify(builder).is(functionIsCaptor.capture());

      var mock = (TestService) functionIsCaptor.getValue().apply(context);
      assertThat(mock.read("1234")).isSameAs(entity1);
      assertThat(mock.read("1234")).isSameAs(entity2);
      assertThatThrownBy(() -> mock.read("1234")).isSameAs(ex);
    }

    @Test
    void shouldNotRegisterStubbingWhenInvokingHasNoResponseConfigured() throws Exception {
      new MockitoExtension<>()
          .getExtension(builder, VARIABLE)
          .mock(TestService.class)
            .invoking(m -> m.read("1234"));  // no will* — ResponseStep ignored

      verify(builder).is(functionIsCaptor.capture());

      var mock = functionIsCaptor.getValue().apply(context);
      assertThat(Mockito.mockingDetails(mock).getStubbings()).isEmpty();
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
