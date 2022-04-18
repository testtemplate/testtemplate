package example.controller;

import org.springframework.util.StringUtils;

public final class PersonNameUtils {

  private PersonNameUtils() {}

  public static String formatName(String firstName, String lastName) {
    if (!StringUtils.hasText(lastName)) {
      throw new IllegalArgumentException("last name must not be null or empty");
    }

    if (!StringUtils.hasText(firstName)) {
      return lastName;
    }

    return lastName + ", " + firstName;
  }
}
