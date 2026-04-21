package io.github.testtemplate.core.runner;

import org.jspecify.annotations.Nullable;

import java.util.Map;

final class TestGroupNameSubstitutor extends AbstractTestNameSubstitutor {

  private static final String SEPARATOR = "...";

  private final String groupName;
  private final String itemName;

  TestGroupNameSubstitutor(String name, RunnerVariableResolver resolver, Map<String, @Nullable Object> metadata) {
    super(resolver, metadata);

    var separatorIndex = name.indexOf(SEPARATOR);
    this.groupName = separatorIndex > -1
        ? name.substring(0, separatorIndex + SEPARATOR.length()).trim()
        : name.trim() + SEPARATOR;
    this.itemName = separatorIndex > -1
        ? name.substring(separatorIndex + SEPARATOR.length()).trim()
        : "";
  }

  public String getTestGroupName() {
    return resolveName(groupName);
  }

  public String getTestItemName() {
    return !itemName.isEmpty() ? itemName : "${exceptions}";
  }
}
