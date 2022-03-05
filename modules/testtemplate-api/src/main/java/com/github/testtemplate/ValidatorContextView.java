package com.github.testtemplate;

public interface ValidatorContextView<R> extends ContextView {

  R result();

  Throwable exception();

}
