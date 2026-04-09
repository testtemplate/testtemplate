package io.github.testtemplate.core.runner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndexesTest {
  @Test
  void testIndexes() {
    var firstLevel = Indexes.index(3);
    assertThat(firstLevel.toString()).isEqualTo("3");

    var secondLevel = firstLevel.subIndex(2);
    assertThat(secondLevel.toString()).isEqualTo("3.2");

    var thirdLevel = secondLevel.subIndex(5);
    assertThat(thirdLevel.toString()).isEqualTo("3.2.5");
  }
}
