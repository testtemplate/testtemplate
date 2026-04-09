package io.github.testtemplate.extension.json;

import io.github.testtemplate.api.builder.AlternativeBuilder;

import com.jayway.jsonpath.JsonPath;
import org.jspecify.annotations.Nullable;

public class JsonExtension<S, R>
    implements AlternativeBuilder.ExtensionFactory<S, R, JsonAlternativeBuilder<S, R>> {

  @Nullable
  private static JsonExtension<?, ?> instance;

  @Override
  public JsonAlternativeBuilder<S, R> getExtension(
      AlternativeBuilder.ExceptStep.MetadataStep<S, R> builder,
      String variable) {
    return new InnerJsonAlternativeBuilder<>(builder, variable);
  }

  @SuppressWarnings("unchecked")
  public static <S, R> JsonExtension<S, R> json() {
    if (instance == null) {
      instance = new JsonExtension<>();
    }

    return (JsonExtension<S, R>) instance;
  }

  private static final class InnerJsonAlternativeBuilder<S, R> implements JsonAlternativeBuilder<S, R> {

    private final AlternativeBuilder.ExceptStep.MetadataStep<S, R> builder;

    private final String variable;

    private InnerJsonAlternativeBuilder(
        AlternativeBuilder.ExceptStep.MetadataStep<S, R> builder,
        String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    @Override
    public ValueStep<S, R> path(String path) {
      return new InnerValueStep(path);
    }

    private final class InnerValueStep implements ValueStep<S, R> {

      private final String path;

      private InnerValueStep(String path) {
        this.path = path;
      }

      @Override
      public AlternativeBuilder.ExceptStep<S, R> is(@Nullable Object value) {
        return builder.is(c -> {
          var originalValue = c.get(variable);
          if (originalValue instanceof String) {
            return JsonPath.parse((String) originalValue).set(path, value).jsonString();
          } else {
            return JsonPath.parse(originalValue).set(path, value).json();
          }
        });
      }

      @Override
      public AlternativeBuilder.ExceptStep<S, R> isAbsent() {
        return builder.is(c -> {
          var originalValue = c.get(variable);
          if (originalValue instanceof String) {
            return JsonPath.parse((String) originalValue).delete(path).jsonString();
          } else {
            return JsonPath.parse(originalValue).delete(path).json();
          }
        });
      }

      @Override
      public AlternativeBuilder.ExceptStep<S, R> hasExtra(@Nullable Object value) {
        return builder.is(c -> {
          var originalValue = c.get(variable);
          if (originalValue instanceof String) {
            return JsonPath.parse((String) originalValue).add(path, value).jsonString();
          } else {
            return JsonPath.parse(originalValue).add(path, value).json();
          }
        });
      }

      @Override
      public AlternativeBuilder.ExceptStep<S, R> hasExtra(String key, @Nullable Object value) {
        return builder.is(c -> {
          var originalValue = c.get(variable);
          if (originalValue instanceof String) {
            return JsonPath.parse((String) originalValue).put(path, key, value).jsonString();
          } else {
            return JsonPath.parse(originalValue).put(path, key, value).json();
          }
        });
      }
    }
  }
}
