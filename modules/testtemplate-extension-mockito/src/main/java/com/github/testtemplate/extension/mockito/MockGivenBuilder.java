package com.github.testtemplate.extension.mockito;

import com.github.testtemplate.DefaultTestTemplateGivenBuilder;

public interface MockGivenBuilder<S> extends DefaultTestTemplateGivenBuilder.Extension<S> {

  <M> MockGivenInvokeBuilder<S, M> mock(Class<? extends M> classToMock);

  <M> MockGivenInvokeBuilder<S, M> use(M mock);

}
