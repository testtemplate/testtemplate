package io.github.testtemplate.api;

public interface ContextExtension extends Context {

  ExtensionStep with(String variable);

  interface ExtensionStep {

    <M extends Extension> M as(ExtensionFactory<M> factory);

  }

  interface ExtensionFactory<M extends Extension> {

    M getExtension(Context context, String variable);

  }

  interface Extension {}

}
