package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.AlternativeTestValidatorBuilder;

public interface MockExpectPostResponseBuilder<S, R, M, T>
    extends MockExpectResponseBuilder<S, R, M, T>, AlternativeTestValidatorBuilder<S, R> {

}
