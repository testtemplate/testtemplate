package example.controller;

import io.github.testtemplate.TestTemplate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static io.github.testtemplate.TestTemplate.BLANK_STRING;
import static io.github.testtemplate.TestTemplate.EMPTY_STRING;
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

        .test("should be formatted with last name when...")
        .sameAsDefault()
        .except("first-name").isNull().or(EMPTY_STRING).or(BLANK_STRING)
        .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown"))

        .test("should throw an exception when last is null")
        .sameAsDefault()
        .except("last-name").isNull().or(EMPTY_STRING).or(BLANK_STRING)
        .then(ctx -> Assertions
            .assertThat(ctx.exception())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("last name must not be null or empty"))

        .suite();
  }
}
