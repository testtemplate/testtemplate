package io.github.testtemplate.extension.json;

import io.github.testtemplate.AlternativeTestTemplateExceptBuilder;
import io.github.testtemplate.AlternativeTestValidatorBuilder;

import com.jayway.jsonpath.JsonPath;

public final class JsonExtension<S, R>
    implements AlternativeTestTemplateExceptBuilder.ExtensionFactory<S, R, JsonExtension.JsonExceptBuilder<S, R>> {

  @Override
  public JsonExceptBuilder<S, R> getExtension(AlternativeTestTemplateExceptBuilder<S, R> builder, String variable) {
    return new JsonExceptBuilder<>(builder, variable);
  }

  public static final class JsonExceptBuilder<S, R> implements AlternativeTestTemplateExceptBuilder.Extension<S, R> {

    private final AlternativeTestTemplateExceptBuilder<S, R> builder;

    private final String variable;

    public JsonExceptBuilder(AlternativeTestTemplateExceptBuilder<S, R> builder, String variable) {
      this.builder = builder;
      this.variable = variable;
    }

    public JsonExceptPathBuilder path(String path) {
      return new JsonExceptPathBuilder(path);
    }

    public final class JsonExceptPathBuilder {

      private final String path;

      public JsonExceptPathBuilder(String path) {
        this.path = path;
      }

      public AlternativeTestValidatorBuilder<S, R> is(Object value) {
        return builder.is(c -> {
          var originalValue = c.get(variable);
          if (originalValue instanceof String) {
            return JsonPath.parse((String) originalValue).set(path, value).jsonString();
          } else {
            return JsonPath.parse(originalValue).set(path, value).json();
          }
        });
      }

      public AlternativeTestValidatorBuilder<S, R> isAbsent() {
        return builder.is(c -> {
          var originalValue = c.get(variable);
          if (originalValue instanceof String) {
            return JsonPath.parse((String) originalValue).delete(path).jsonString();
          } else {
            return JsonPath.parse(originalValue).delete(path).json();
          }
        });
      }

      public AlternativeTestValidatorBuilder<S, R> hasExtra(Object value) {
        return builder.is(c -> {
          var originalValue = c.get(variable);
          if (originalValue instanceof String) {
            return JsonPath.parse((String) originalValue).add(path, value).jsonString();
          } else {
            return JsonPath.parse(originalValue).add(path, value).json();
          }
        });
      }

      public AlternativeTestValidatorBuilder<S, R> hasExtra(String key, Object value) {
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
