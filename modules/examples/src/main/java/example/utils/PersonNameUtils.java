package example.utils;

public final class PersonNameUtils {

  private PersonNameUtils() {}

  public static String formatName(String firstName, String middleName, String lastName) {
    if (lastName == null || lastName.isBlank()) {
      throw new IllegalArgumentException("last name cannot be null, empty or blank");
    }

    if (firstName == null || firstName.isBlank()) {
      return lastName;
    }

    if (middleName == null || middleName.isBlank()) {
      return lastName + ", " + firstName;
    }

    return lastName + ", " + firstName + " " + middleName;
  }
}
