package io.github.testtemplate;

public interface AlternativeTestTemplatePreBuilder<S, R> extends AlternativeTestTemplateBuilder<S, R> {

  AlternativeTestTemplatePreBuilder<S, R> disabled(String reason);

  default AlternativeTestTemplatePreBuilder<S, R> disabled() {
    return disabled("unknown reason");
  }
}
