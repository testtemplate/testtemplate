package io.github.testtemplate.api.builder;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.function.ExceptionalConsumer;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public interface DefaultBuilder<S> {

  TestMetadataStep<S> defaultTest(String name);

  interface TestMetadataStep<S> extends SetupStep<S> {

    TestMetadataStep<S> metadata(String key, @Nullable Object value);

    default TestMetadataStep<S> disabled(String reason) {
      return this
          .metadata(Metadata.Test.DISABLED, true)
          .metadata(Metadata.Test.DISABLED_REASON, Objects.requireNonNull(reason));
    }

    default TestMetadataStep<S> disabled() {
      return disabled("unknown reason");
    }
  }

  interface SetupStep<S> extends GivenStep<S> {

    GivenStep<S> setUp(ExceptionalConsumer<SetupBuilder> setup);

  }

  interface GivenStep<S> extends WhenStep<S> {

    ExtensionStep<S> given(String variable);

    interface ExtensionStep<S> extends MetadataStep<S> {

      <M extends Extension<S>> M as(ExtensionFactory<S, M> factory);

    }

    interface MetadataStep<S> extends ValueStep<S> {

      MetadataStep<S> metadata(String key, @Nullable Object value);

      default MetadataStep<S> preload() {
        return metadata(Metadata.Variable.PRELOAD, true);
      }
    }

    interface ValueStep<S> {

      GivenStep<S> is(ExceptionalFunction<ContextGiven, ?> value);

      default GivenStep<S> is(ExceptionalSupplier<?> value) {
        return is(ctx -> value.get());
      }

      default GivenStep<S> is(@Nullable Object value) {
        return is(ctx -> value);
      }

      default GivenStep<S> isNull() {
        return is(ctx -> null);
      }
    }
  }

  interface WhenStep<S> {

    <R> ThenStep<S, R> when(ExceptionalFunction<ContextGiven, R> template);

  }

  interface ThenStep<S, R> {

    AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator);

  }

  interface ExtensionFactory<S, M extends Extension<S>> {

    M getExtension(GivenStep.MetadataStep<S> builder, String variable);

  }

  interface Extension<S> {}

}
