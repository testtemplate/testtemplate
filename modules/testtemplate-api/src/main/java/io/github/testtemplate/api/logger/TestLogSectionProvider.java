package io.github.testtemplate.api.logger;

import java.util.List;

public interface TestLogSectionProvider {

  String name();

  List<String> lines(TestLogSnapshot snapshot);

}
