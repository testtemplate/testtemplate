package io.github.testtemplate.core.logger;

import io.github.testtemplate.api.logger.TestLogSectionProvider;
import io.github.testtemplate.api.logger.TestLogSnapshot;

import java.util.List;

class TemplateSectionProvider implements TestLogSectionProvider {

  @Override
  public String name() {
    return "TEMPLATE_SOURCE";
  }

  @Override
  public List<String> lines(TestLogSnapshot snapshot) {
    var source = snapshot.templateSource();
    if (source == null) {
      return List.of();
    }
    return List.of(
        "Template:",
        "  " + source.getFileName() + ":" + source.getLineNumber()
    );
  }

}
