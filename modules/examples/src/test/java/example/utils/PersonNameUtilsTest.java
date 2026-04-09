package example.utils;

import io.github.testtemplate.TestBuilder;
import io.github.testtemplate.api.builder.SetupBuilder;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PersonNameUtilsTest {

  @TestFactory
  Stream<DynamicNode> formatName() {
    return TestBuilder
        .defaultTest("should be formatted beginning with last name followed by first name and middle name")
        .setUp(PersonNameUtilsTest::setUpMiddleName)
        .given("first-name").is("Alice")
        .given("last-name").is("Brown")
        .when(ctx -> PersonNameUtils.formatName(ctx.get("first-name"), ctx.get("middle-name"), ctx.get("last-name")))
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown, Alice J"))

        .test("should be formatted with other names")
        .sameAsDefault()
        .except("first-name").is("Robert")
        .except("middle-name").is("K")
        .except("last-name").is("Gravel")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Gravel, Robert K"))

        .test("should be formatted with yet another name >>${exception-values}<<")
        .sameAsDefault()
        .except("first-name", "middle-name", "last-name")
        .are("John", null, "Snow")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Snow, John"))

        .test("should respected doubled middle name")
        .sameAsDefault()
        .except("middle-name").is(ctx -> ctx.get("middle-name") + "" + ctx.get("middle-name"))
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown, Alice JJ"))

        .test("should not show middle name when... middle name is ${middle-name}")
        .sameAsDefault()
        .except("middle-name").isNull().or("").or(" ")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown, Alice"))

        .test("should show only last name... fn is ${first-name}")
        .sameAsDefault()
        .except("first-name").isNull().or("").or("  ")
        .except("middle-name").isNull().or("Z")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown"))

        .test("should throw exception when last name is absent")
        .sameAsDefault()
        .except("last-name").isNull().or("").or("                ")
        .then(ctx -> assertThat(ctx.exception()).isInstanceOf(IllegalArgumentException.class))

        .suite();
  }

  private static void setUpMiddleName(SetupBuilder builder) {
    builder.given("middle-name").is("J");
  }
}
