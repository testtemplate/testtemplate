package io.github.testtemplate.api.builder;

public final class Metadata {

  private Metadata() {}

  public static final class Test {

    public static final String DISABLED = "io.github.testtemplate.test.disabled";
    public static final String DISABLED_REASON = "io.github.testtemplate.test.disabled-reason";

    private Test() {}

  }

  public static final class Variable {

    public static final String PRELOAD = "io.github.testtemplate.variable.preload";

    private Variable() {}

  }
}
