package com.github.testtemplate.extension.json;

import com.github.testtemplate.AlternativeTestTemplateExceptBuilder;
import com.github.testtemplate.ContextView;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonExtensionTest {

  private static final String VARIABLE = "test-json";

  @Mock
  private AlternativeTestTemplateExceptBuilder<Object, Object> builder;

  @Mock
  private ContextView context;

  @Captor
  private ArgumentCaptor<Function<ContextView, ?>> functionIsCaptor;

  @Nested
  class OriginalValueAsString {
    @Test
    void shouldReplaceValue() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.title")
          .is("welcome");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson("{\"title\":\"hello\",\"body\":\"bla bla bla\"}");
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo("{\"title\":\"welcome\",\"body\":\"bla bla bla\"}");
    }

    @Test
    void shouldReplaceValueWithNull() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.title")
          .is(null);

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson("{\"title\":\"hello\",\"body\":\"bla bla bla\"}");
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo("{\"title\":null,\"body\":\"bla bla bla\"}");
    }

    @Test
    void shouldRemoveField() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body")
          .isAbsent();

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson("{\"title\":\"hello\",\"body\":\"bla bla bla\"}");
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo("{\"title\":\"hello\"}");
    }

    @Test
    void shouldAddField() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$")
          .hasExtra("extra", "value");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson("{\"title\":\"hello\",\"body\":\"bla bla bla\"}");
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo("{\"title\":\"hello\",\"body\":\"bla bla bla\",\"extra\":\"value\"}");
    }

    @Test
    void shouldAddArrayElement() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body")
          .hasExtra("four");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson("{\"title\":\"hello\",\"body\":[\"one\",\"two\",\"three\"]}");
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo("{\"title\":\"hello\",\"body\":[\"one\",\"two\",\"three\",\"four\"]}");
    }

    @Test
    void shouldReplaceArrayElement() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body[1]")
          .is("zero");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson("{\"title\":\"hello\",\"body\":[\"one\",\"two\",\"three\"]}");
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo("{\"title\":\"hello\",\"body\":[\"one\",\"zero\",\"three\"]}");
    }

    @Test
    void shouldRemoveArrayElement() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body[1]")
          .isAbsent();

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson("{\"title\":\"hello\",\"body\":[\"one\",\"two\",\"three\"]}");
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo("{\"title\":\"hello\",\"body\":[\"one\",\"three\"]}");
    }
  }

  @Nested
  class OriginalValueAsMap {
    @Test
    void shouldReplaceValue() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.title")
          .is("welcome");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson(map("title", "hello", "body", "bla bla bla"));
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo(map("title", "welcome", "body", "bla bla bla"));
    }

    @Test
    void shouldReplaceValueWithNull() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.title")
          .is(null);

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson(map("title", "hello", "body", "bla bla bla"));
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo(map("title", null, "body", "bla bla bla"));
    }

    @Test
    void shouldRemoveField() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body")
          .isAbsent();

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson(map("title", "hello", "body", "bla bla bla"));
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo(map("title", "hello"));
    }

    @Test
    void shouldAddField() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$")
          .hasExtra("extra", "value");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson(map("title", "hello", "body", "bla bla bla"));
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo(map("title", "hello", "body", "bla bla bla", "extra", "value"));
    }

    @Test
    void shouldAddArrayElement() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body")
          .hasExtra("four");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson(map("title", "hello", "body", list("one", "two", "three")));
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo(map("title", "hello", "body", list("one", "two", "three", "four")));
    }

    @Test
    void shouldReplaceArrayElement() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body[1]")
          .is("zero");

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson(map("title", "hello", "body", list("one", "two", "three")));
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo(map("title", "hello", "body", list("one", "zero", "three")));
    }

    @Test
    void shouldRemoveArrayElement() {
      new JsonExtension<>()
          .getExtension(builder, VARIABLE)
          .path("$.body[1]")
          .isAbsent();

      verify(builder).is(functionIsCaptor.capture());

      givenOriginalJson(map("title", "hello", "body", list("one", "two", "three")));
      var result = functionIsCaptor.getValue().apply(context);

      assertThat(result).isEqualTo(map("title", "hello", "body", list("one", "three")));
    }
  }

  private void givenOriginalJson(Object json) {
    when(context.get(VARIABLE)).thenReturn(json);
  }

  private static Map<String, Object> map(String key1, Object value1) {
    Map<String, Object> map = new HashMap<>();
    map.put(key1, value1);
    return map;
  }

  private static Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
    Map<String, Object> map = new HashMap<>();
    map.put(key1, value1);
    map.put(key2, value2);
    return map;
  }

  private static Map<String, Object> map(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
    Map<String, Object> map = new HashMap<>();
    map.put(k1, v1);
    map.put(k2, v2);
    map.put(k3, v3);
    return map;
  }

  private static List<Object> list(Object v1, Object v2) {
    List<Object> list = new ArrayList<>();
    list.add(v1);
    list.add(v2);
    return list;
  }

  private static List<Object> list(Object v1, Object v2, Object v3) {
    List<Object> list = new ArrayList<>();
    list.add(v1);
    list.add(v2);
    list.add(v3);
    return list;
  }

  private static List<Object> list(Object v1, Object v2, Object v3, Object v4) {
    List<Object> list = new ArrayList<>();
    list.add(v1);
    list.add(v2);
    list.add(v3);
    list.add(v4);
    return list;
  }
}
