package io.github.testtemplate.api.builder;

import io.github.testtemplate.api.ContextGiven;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

import org.jspecify.annotations.Nullable;

public interface SetupBuilder {

  SetupBuilder metadata(String key, @Nullable Object value);

  ExtensionStep given(String variable);

  interface GivenStep {

    ExtensionStep given(String variable);

  }

  interface ExtensionStep extends MetadataStep {

    <M extends Extension> M as(ExtensionFactory<M> factory);

  }

  interface MetadataStep extends ValueStep {

    MetadataStep metadata(String key, @Nullable Object value);

    default MetadataStep preload() {
      return metadata(Metadata.Variable.PRELOAD, true);
    }
  }

  interface ValueStep {

    GivenStep is(ExceptionalFunction<ContextGiven, ?> value);

    default GivenStep is(ExceptionalSupplier<?> value) {
      return is(ctx -> value.get());
    }

    default GivenStep is(@Nullable Object value) {
      return is(ctx -> value);
    }

    default GivenStep isNull() {
      return is(ctx -> null);
    }
  }

  interface ExtensionFactory<M extends Extension> {

    M getExtension(MetadataStep builder, String variable);

  }

  interface Extension {}

}
