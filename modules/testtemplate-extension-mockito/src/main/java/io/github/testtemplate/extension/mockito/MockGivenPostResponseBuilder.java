package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.DefaultTestTemplateBuilder;

public interface MockGivenPostResponseBuilder<S, M, T>
    extends MockGivenResponseBuilder<S, M, T>, MockGivenInvokeBuilder<S, M>, DefaultTestTemplateBuilder<S> {
}
