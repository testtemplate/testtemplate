package io.github.testtemplate.core.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import io.github.testtemplate.api.logger.TestLogSectionProvider;
import io.github.testtemplate.api.logger.TestLogSnapshot;

class ResultSectionProvider implements TestLogSectionProvider {

  @Override
  public String name() {
    return "RESULT";
  }

  @Override
  public List<String> lines(TestLogSnapshot snapshot) {
    var exceptionOpt = snapshot.exception();
    if (exceptionOpt.isPresent()) {
      var exception = exceptionOpt.get();
      List<String> lines = new ArrayList<>();
      lines.add("Exception Thrown:");
      var writer = new StringWriter();
      exception.printStackTrace(new PrintWriter(writer));
      String stackTrace = writer.toString().replace("\t", "    ").indent(2);
      // indent() appends a newline; we trim and re-split to get individual lines
      for (String line : stackTrace.stripTrailing().split("\n")) {
        lines.add(line);
      }
      return lines;
    }
    return List.of(
        "Result:",
        "  " + snapshot.result().orElse(null)
    );
  }

}
