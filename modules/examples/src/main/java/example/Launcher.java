package example;

import org.springframework.boot.SpringApplication;

public final class Launcher {

  private Launcher() {}

  public static void main(String[] arguments) {
    SpringApplication.run(Application.class, arguments);
  }
}
