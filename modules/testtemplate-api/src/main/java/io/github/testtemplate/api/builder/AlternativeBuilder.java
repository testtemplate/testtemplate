package io.github.testtemplate.api.builder;

import io.github.testtemplate.api.Context;
import io.github.testtemplate.api.ContextResult;
import io.github.testtemplate.api.function.ExceptionalConsumer;
import io.github.testtemplate.api.function.ExceptionalFunction;
import io.github.testtemplate.api.function.ExceptionalSupplier;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface AlternativeBuilder<S, R> extends SuiteBuilder<S> {

  TestMetadataStep<S, R> test(String name);

  interface TestMetadataStep<S, R> extends SameStep<S, R> {

    TestMetadataStep<S, R> metadata(String key, @Nullable Object value);

    default TestMetadataStep<S, R> disabled(String reason) {
      return this
          .metadata(Metadata.Test.DISABLED, true)
          .metadata(Metadata.Test.DISABLED_REASON, Objects.requireNonNull(reason));
    }

    default TestMetadataStep<S, R> disabled() {
      return disabled("unknown reason");
    }
  }

  interface SameStep<S, R> {

    ExceptStep<S, R> sameAsDefault();

  }

  interface ExceptStep<S, R> extends ThenStep<S, R> {

    ExtensionStep<S, R> except(String variable);

    Value2Step<S, R> except(String variable1, String variable2);

    Value3Step<S, R> except(String variable1, String variable2, String variable3);

    Value4Step<S, R> except(String variable1, String variable2, String variable3, String variable4);

    Value5Step<S, R> except(String variable1, String variable2, String variable3, String variable4, String variable5);

    ValueNStep<S, R> except(List<String> variables);

    interface ExtensionStep<S, R> extends MetadataStep<S, R> {

      <M extends Extension<S, R>> M as(ExtensionFactory<S, R, M> factory);

    }

    interface MetadataStep<S, R> extends ValueStep<S, R> {

      MetadataStep<S, R> metadata(String key, @Nullable Object value);

    }

    interface ValueStep<S, R> {

      PostStep<S, R> is(ExceptionalFunction<Context, ?> value);

      default PostStep<S, R> is(ExceptionalSupplier<?> value) {
        return is(ctx -> value.get());
      }

      default PostStep<S, R> is(@Nullable Object value) {
        return is(ctx -> value);
      }

      default PostStep<S, R> isNull() {
        return is(ctx -> null);
      }
    }

    // <editor-fold defaultstate="collapsed" desc="ValueNStep">

    interface ValueNStep<S, R> {

      PostNStep<S, R> are(List<ExceptionalFunction<Context, ?>> values);

    }

    interface Value2Step<S, R> {

      Post2Step<S, R> are(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2);

      default Post2Step<S, R> are(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2) {
        return are(
            ctx -> value1.get(),
            ctx -> value2.get());
      }

      default Post2Step<S, R> are(
          @Nullable Object value1,
          @Nullable Object value2) {
        return are(
            ctx -> value1,
            ctx -> value2);
      }
    }

    interface Value3Step<S, R> {

      Post3Step<S, R> are(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2,
          ExceptionalFunction<Context, ?> value3);

      default Post3Step<S, R> are(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2,
          ExceptionalSupplier<?> value3) {
        return are(
            ctx -> value1.get(),
            ctx -> value2.get(),
            ctx -> value3.get());
      }

      default Post3Step<S, R> are(
          @Nullable Object value1,
          @Nullable Object value2,
          @Nullable Object value3) {
        return are(
            ctx -> value1,
            ctx -> value2,
            ctx -> value3);
      }
    }

    interface Value4Step<S, R> {

      Post4Step<S, R> are(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2,
          ExceptionalFunction<Context, ?> value3,
          ExceptionalFunction<Context, ?> value4);

      default Post4Step<S, R> are(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2,
          ExceptionalSupplier<?> value3,
          ExceptionalSupplier<?> value4) {
        return are(
            ctx -> value1.get(),
            ctx -> value2.get(),
            ctx -> value3.get(),
            ctx -> value4.get());
      }

      default Post4Step<S, R> are(
          @Nullable Object value1,
          @Nullable Object value2,
          @Nullable Object value3,
          @Nullable Object value4) {
        return are(
            ctx -> value1,
            ctx -> value2,
            ctx -> value3,
            ctx -> value4);
      }
    }

    interface Value5Step<S, R> {

      Post5Step<S, R> are(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2,
          ExceptionalFunction<Context, ?> value3,
          ExceptionalFunction<Context, ?> value4,
          ExceptionalFunction<Context, ?> value5);

      default Post5Step<S, R> are(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2,
          ExceptionalSupplier<?> value3,
          ExceptionalSupplier<?> value4,
          ExceptionalSupplier<?> value5) {
        return are(
            ctx -> value1.get(),
            ctx -> value2.get(),
            ctx -> value3.get(),
            ctx -> value4.get(),
            ctx -> value5.get());
      }

      default Post5Step<S, R> are(
          @Nullable Object value1,
          @Nullable Object value2,
          @Nullable Object value3,
          @Nullable Object value4,
          @Nullable Object value5) {
        return are(
            ctx -> value1,
            ctx -> value2,
            ctx -> value3,
            ctx -> value4,
            ctx -> value5);
      }
    }

    // </editor-fold>

    interface PostStep<S, R> extends ExceptStep<S, R> {

      PostStep<S, R> or(ExceptionalFunction<Context, ?> value);

      default PostStep<S, R> or(ExceptionalSupplier<?> value) {
        return or(ctx -> value.get());
      }

      default PostStep<S, R> or(@Nullable Object value) {
        return or(ctx -> value);
      }

      default PostStep<S, R> orNull() {
        return or(ctx -> null);
      }
    }

    // <editor-fold defaultstate="collapsed" desc="PostNStep">

    interface PostNStep<S, R> extends ExceptStep<S, R> {

      PostNStep<S, R> or(List<ExceptionalFunction<Context, ?>> values);

    }

    interface Post2Step<S, R> extends ExceptStep<S, R> {

      Post2Step<S, R> or(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2);

      default Post2Step<S, R> or(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2) {
        return or(
            ctx -> value1.get(),
            ctx -> value2.get());
      }

      default Post2Step<S, R> or(
          @Nullable Object value1,
          @Nullable Object value2) {
        return or(
            ctx -> value1,
            ctx -> value2);
      }
    }

    interface Post3Step<S, R> extends ExceptStep<S, R> {

      Post3Step<S, R> or(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2,
          ExceptionalFunction<Context, ?> value3);

      default Post3Step<S, R> or(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2,
          ExceptionalSupplier<?> value3) {
        return or(
            ctx -> value1.get(),
            ctx -> value2.get(),
            ctx -> value3.get());
      }

      default Post3Step<S, R> or(
          @Nullable Object value1,
          @Nullable Object value2,
          @Nullable Object value3) {
        return or(
            ctx -> value1,
            ctx -> value2,
            ctx -> value3);
      }
    }

    interface Post4Step<S, R> extends ExceptStep<S, R> {

      Post4Step<S, R> or(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2,
          ExceptionalFunction<Context, ?> value3,
          ExceptionalFunction<Context, ?> value4);

      default Post4Step<S, R> or(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2,
          ExceptionalSupplier<?> value3,
          ExceptionalSupplier<?> value4) {
        return or(
            ctx -> value1.get(),
            ctx -> value2.get(),
            ctx -> value3.get(),
            ctx -> value4.get());
      }

      default Post4Step<S, R> or(
          @Nullable Object value1,
          @Nullable Object value2,
          @Nullable Object value3,
          @Nullable Object value4) {
        return or(
            ctx -> value1,
            ctx -> value2,
            ctx -> value3,
            ctx -> value4);
      }
    }

    interface Post5Step<S, R> extends ExceptStep<S, R> {

      Post5Step<S, R> or(
          ExceptionalFunction<Context, ?> value1,
          ExceptionalFunction<Context, ?> value2,
          ExceptionalFunction<Context, ?> value3,
          ExceptionalFunction<Context, ?> value4,
          ExceptionalFunction<Context, ?> value5);

      default Post5Step<S, R> or(
          ExceptionalSupplier<?> value1,
          ExceptionalSupplier<?> value2,
          ExceptionalSupplier<?> value3,
          ExceptionalSupplier<?> value4,
          ExceptionalSupplier<?> value5) {
        return or(
            ctx -> value1.get(),
            ctx -> value2.get(),
            ctx -> value3.get(),
            ctx -> value4.get(),
            ctx -> value5.get());
      }

      default Post5Step<S, R> or(
          @Nullable Object value1,
          @Nullable Object value2,
          @Nullable Object value3,
          @Nullable Object value4,
          @Nullable Object value5) {
        return or(
            ctx -> value1,
            ctx -> value2,
            ctx -> value3,
            ctx -> value4,
            ctx -> value5);
      }
    }

    // </editor-fold>

  }

  interface ThenStep<S, R> {

    AlternativeBuilder<S, R> then(ExceptionalConsumer<ContextResult<R>> validator);

  }

  interface ExtensionFactory<S, R, M extends Extension<S, R>> {

    M getExtension(ExceptStep.MetadataStep<S, R> builder, String variable);

  }

  interface Extension<S, R> {}
}
