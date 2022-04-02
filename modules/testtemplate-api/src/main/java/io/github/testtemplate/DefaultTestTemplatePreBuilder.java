package io.github.testtemplate;

public interface DefaultTestTemplatePreBuilder<S> extends DefaultTestTemplateBuilder<S> {

  DefaultTestTemplatePreBuilder<S> disabled(String reason);

  default DefaultTestTemplatePreBuilder<S> disabled() {
    return disabled("unknown reason");
  }
}
