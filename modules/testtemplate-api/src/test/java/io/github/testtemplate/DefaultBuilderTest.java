package io.github.testtemplate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

@ExtendWith(MockitoExtension.class)
class DefaultBuilderTest {

  @Nested
  class TestMetadataStepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DefaultBuilder.TestMetadataStep<Object> step;

    @Test
    void shouldCallDisabledWithUnknownReasonWhenNoReasonProvided() {
      Mockito.doReturn(step).when(step).metadata(Mockito.anyString(), Mockito.any());

      step.disabled();

      Mockito.verify(step).disabled("unknown reason");
    }

    @Test
    void shouldSetDisabledAndReasonMetadataWhenReasonProvided() {
      Mockito.doReturn(step).when(step).metadata(Mockito.anyString(), Mockito.any());

      step.disabled("some reason");

      Mockito.verify(step).metadata(Metadata.Test.DISABLED, true);
      Mockito.verify(step).metadata(Metadata.Test.DISABLED_REASON, "some reason");
    }
  }

  @Nested
  class MetadataStepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DefaultBuilder.GivenStep.MetadataStep<Object> step;

    @Test
    void shouldSetPreloadMetadataWhenPreloadCalled() {
      Mockito.doReturn(step).when(step).metadata(Mockito.anyString(), Mockito.any());

      step.preload();

      Mockito.verify(step).metadata(Metadata.Variable.PRELOAD, true);
    }
  }

  @Nested
  class ValueStepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DefaultBuilder.GivenStep.ValueStep<Object> step;

    @Test
    void shouldWrapSupplierInFunctionWhenSupplierProvided() throws Exception {
      ExceptionalSupplier<String> supplier = () -> "supplied";
      ArgumentCaptor<ExceptionalFunction<ContextGiven, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).is(captor.capture());

      step.is(supplier);

      Assertions.assertThat(captor.getValue().apply(null)).isEqualTo("supplied");
    }

    @Test
    void shouldWrapObjectInFunctionWhenObjectProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<ContextGiven, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).is(captor.capture());

      step.is("fixed value");

      Assertions.assertThat(captor.getValue().apply(null)).isEqualTo("fixed value");
    }

    @Test
    void shouldWrapNullInFunctionWhenIsNullCalled() throws Exception {
      ArgumentCaptor<ExceptionalFunction<ContextGiven, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).is(captor.capture());

      step.isNull();

      Assertions.assertThat(captor.getValue().apply(null)).isNull();
    }
  }
}
