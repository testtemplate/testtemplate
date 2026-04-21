package io.github.testtemplate.extension.mockito;

import io.github.testtemplate.api.logger.TestLogSectionProvider;
import io.github.testtemplate.api.logger.TestLogSnapshot;

import org.mockito.Mockito;
import org.mockito.stubbing.Stubbing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MockitoLogSectionProvider implements TestLogSectionProvider {

  @Override
  public String name() {
    return "MOCKITO_STUBS";
  }

  @Override
  public List<String> lines(TestLogSnapshot snapshot) {
    List<String> lines = new ArrayList<>();
    lines.add("Mockito Stubs:");
    snapshot.variables().forEach((varName, variable) -> {
      if (Boolean.TRUE.equals(variable.getMetadata(MockitoMetadata.Variable.IS_MOCK))) {
        Object mock = variable.getValue();
        Collection<Stubbing> stubbings = Mockito.mockingDetails(mock).getStubbings();
        if (!stubbings.isEmpty()) {
          lines.add("  " + varName + ":");
          stubbings.forEach(s -> lines.add("    " + s.getInvocation()));
        }
      }
    });
    if (lines.size() == 1) {
      return List.of();
    }
    return lines;
  }

}
