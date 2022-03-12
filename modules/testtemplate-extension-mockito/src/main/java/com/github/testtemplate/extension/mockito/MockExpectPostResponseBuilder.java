package com.github.testtemplate.extension.mockito;

import com.github.testtemplate.AlternativeTestValidatorBuilder;

public interface MockExpectPostResponseBuilder<S, R, M, T>
    extends MockExpectResponseBuilder<S, R, M, T>, AlternativeTestValidatorBuilder<S, R> {

}
