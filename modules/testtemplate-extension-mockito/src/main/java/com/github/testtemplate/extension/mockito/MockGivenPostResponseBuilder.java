package com.github.testtemplate.extension.mockito;

import com.github.testtemplate.DefaultTestTemplateBuilder;

public interface MockGivenPostResponseBuilder<S, M, T>
    extends MockGivenResponseBuilder<S, M, T>, MockGivenInvokeBuilder<S, M>, DefaultTestTemplateBuilder<S> {
}
