package io.github.testtemplate.api.builder;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlternativeBuilderTest {

  @Nested
  class TestMetadataStepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.TestMetadataStep<Object, Object> step;

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
  class ValueStepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.ValueStep<Object, Object> step;

    @Test
    void shouldWrapSupplierInFunctionWhenSupplierProvided() throws Exception {
      ExceptionalSupplier<String> supplier = () -> "supplied";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).is(captor.capture());

      step.is(supplier);

      Assertions.assertThat(captor.getValue().apply(null)).isEqualTo("supplied");
    }

    @Test
    void shouldWrapObjectInFunctionWhenObjectProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).is(captor.capture());

      step.is("fixed value");

      Assertions.assertThat(captor.getValue().apply(null)).isEqualTo("fixed value");
    }

    @Test
    void shouldWrapNullInFunctionWhenIsNullCalled() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).is(captor.capture());

      step.isNull();

      Assertions.assertThat(captor.getValue().apply(null)).isNull();
    }
  }

  @Nested
  class PostStepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.PostStep<Object, Object> step;

    @Test
    void shouldWrapSupplierInFunctionWhenSupplierProvided() throws Exception {
      ExceptionalSupplier<String> supplier = () -> "supplied";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor.capture());

      step.or(supplier);

      Assertions.assertThat(captor.getValue().apply(null)).isEqualTo("supplied");
    }

    @Test
    void shouldWrapObjectInFunctionWhenObjectProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor.capture());

      step.or("fixed value");

      Assertions.assertThat(captor.getValue().apply(null)).isEqualTo("fixed value");
    }

    @Test
    void shouldWrapNullInFunctionWhenOrNullCalled() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor.capture());

      step.orNull();

      Assertions.assertThat(captor.getValue().apply(null)).isNull();
    }
  }

  @Nested
  class Value2StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Value2Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).are(captor1.capture(), captor2.capture());

      step.are(supplier1, supplier2);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).are(captor1.capture(), captor2.capture());

      step.are("first", "second");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
    }
  }

  @Nested
  class Post2StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Post2Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor1.capture(), captor2.capture());

      step.or(supplier1, supplier2);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor1.capture(), captor2.capture());

      step.or("first", "second");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
    }
  }

  @Nested
  class Value3StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Value3Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ExceptionalSupplier<String> supplier3 = () -> "third";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).are(captor1.capture(), captor2.capture(), captor3.capture());

      step.are(supplier1, supplier2, supplier3);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).are(captor1.capture(), captor2.capture(), captor3.capture());

      step.are("first", "second", "third");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
    }
  }

  @Nested
  class Post3StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Post3Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ExceptionalSupplier<String> supplier3 = () -> "third";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor1.capture(), captor2.capture(), captor3.capture());

      step.or(supplier1, supplier2, supplier3);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor1.capture(), captor2.capture(), captor3.capture());

      step.or("first", "second", "third");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
    }
  }

  @Nested
  class Value4StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Value4Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ExceptionalSupplier<String> supplier3 = () -> "third";
      ExceptionalSupplier<String> supplier4 = () -> "fourth";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).are(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture());

      step.are(supplier1, supplier2, supplier3, supplier4);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).are(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture());

      step.are("first", "second", "third", "fourth");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
    }
  }

  @Nested
  class Post4StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Post4Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ExceptionalSupplier<String> supplier3 = () -> "third";
      ExceptionalSupplier<String> supplier4 = () -> "fourth";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture());

      step.or(supplier1, supplier2, supplier3, supplier4);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step).or(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture());

      step.or("first", "second", "third", "fourth");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
    }
  }

  @Nested
  class Value5StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Value5Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ExceptionalSupplier<String> supplier3 = () -> "third";
      ExceptionalSupplier<String> supplier4 = () -> "fourth";
      ExceptionalSupplier<String> supplier5 = () -> "fifth";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor5 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step)
          .are(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture(), captor5.capture());

      step.are(supplier1, supplier2, supplier3, supplier4, supplier5);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
      Assertions.assertThat(captor5.getValue().apply(null)).isEqualTo("fifth");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor5 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step)
          .are(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture(), captor5.capture());

      step.are("first", "second", "third", "fourth", "fifth");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
      Assertions.assertThat(captor5.getValue().apply(null)).isEqualTo("fifth");
    }
  }

  @Nested
  class Post5StepTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AlternativeBuilder.ExceptStep.Post5Step<Object, Object> step;

    @Test
    void shouldWrapSuppliersInFunctionsWhenSuppliersProvided() throws Exception {
      ExceptionalSupplier<String> supplier1 = () -> "first";
      ExceptionalSupplier<String> supplier2 = () -> "second";
      ExceptionalSupplier<String> supplier3 = () -> "third";
      ExceptionalSupplier<String> supplier4 = () -> "fourth";
      ExceptionalSupplier<String> supplier5 = () -> "fifth";
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor5 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step)
          .or(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture(), captor5.capture());

      step.or(supplier1, supplier2, supplier3, supplier4, supplier5);

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
      Assertions.assertThat(captor5.getValue().apply(null)).isEqualTo("fifth");
    }

    @Test
    void shouldWrapObjectsInFunctionsWhenObjectsProvided() throws Exception {
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor1 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor2 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor3 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor4 = ArgumentCaptor.captor();
      ArgumentCaptor<ExceptionalFunction<Context, ?>> captor5 = ArgumentCaptor.captor();
      Mockito.doReturn(null).when(step)
          .or(captor1.capture(), captor2.capture(), captor3.capture(), captor4.capture(), captor5.capture());

      step.or("first", "second", "third", "fourth", "fifth");

      Assertions.assertThat(captor1.getValue().apply(null)).isEqualTo("first");
      Assertions.assertThat(captor2.getValue().apply(null)).isEqualTo("second");
      Assertions.assertThat(captor3.getValue().apply(null)).isEqualTo("third");
      Assertions.assertThat(captor4.getValue().apply(null)).isEqualTo("fourth");
      Assertions.assertThat(captor5.getValue().apply(null)).isEqualTo("fifth");
    }
  }
}
