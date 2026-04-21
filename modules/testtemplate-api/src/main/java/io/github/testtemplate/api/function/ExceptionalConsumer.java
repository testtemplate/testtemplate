package io.github.testtemplate.api.function;

@FunctionalInterface
public interface ExceptionalConsumer<T> {

  void accept(T t) throws Exception;

}
