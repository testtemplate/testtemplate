package io.github.testtemplate;

@FunctionalInterface
public interface ContextualTemplate<R> {

  R run(Context context) throws Exception;

}
