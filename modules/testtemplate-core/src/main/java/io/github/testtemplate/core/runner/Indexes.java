package io.github.testtemplate.core.runner;

import java.util.Arrays;
import java.util.stream.Collectors;

final class Indexes {

  private final int[] indexes;

  private Indexes(int index) {
    this.indexes = new int[] { index };
  }

  private Indexes(int[] indexes, int index) {
    this.indexes = Arrays.copyOf(indexes, indexes.length + 1);
    this.indexes[indexes.length] = index;
  }

  public Indexes subIndex(int index) {
    return new Indexes(indexes, index);
  }

  @Override
  public String toString() {
    return Arrays.stream(indexes).mapToObj(String::valueOf).collect(Collectors.joining("."));
  }

  public static Indexes index(int index) {
    return new Indexes(index);
  }
}
