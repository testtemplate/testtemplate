package example.controller;

import io.github.testtemplate.TestTemplate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PersonNameUtilsTest {

  @TestFactory
  Stream<DynamicNode> formatName() {
    return TestTemplate
        .defaultTest("should be formatted beginning with last name followed with first name")
        .given("first-name").is("Alice")
        .given("last-name").is("Brown")
        .when(ctx -> PersonNameUtils.formatName(ctx.get("first-name"), ctx.get("last-name")))
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown, Alice"))

        .test("should be formatted with last name when first name is null")
        .sameAsDefault()
        .except("first-name").isNull()
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown"))

        .test("should be formatted with last name when first name is empty")
        .sameAsDefault()
        .except("first-name").is("")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown"))

        .test("should be formatted with last name when first name is blank")
        .sameAsDefault()
        .except("first-name").is("  ")
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown"))

        .test("should throw an exception when last is null")
        .sameAsDefault()
        .except("last-name").isNull()
        .then(ctx -> Assertions
            .assertThat(ctx.exception())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("last name must not be null or empty"))

        .test("should throw an exception when last is empty")
        .sameAsDefault()
        .except("last-name").is("")
        .then(ctx -> Assertions
            .assertThat(ctx.exception())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("last name must not be null or empty"))

        .test("should throw an exception when last is blank")
        .sameAsDefault()
        .except("last-name").is("  ")
        .then(ctx -> Assertions
            .assertThat(ctx.exception())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("last name must not be null or empty"))

        .suite();
  }
}
