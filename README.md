# TestTemplate

[![Build Status](https://github.com/testtemplate/testtemplate/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/testtemplate/testtemplate/actions)
[![License](https://img.shields.io/github/license/testtemplate/testtemplate)](https://github.com/testtemplate/testtemplate/blob/master/LICENSE)

TestTemplate is a java library that provides a fluent builder to write a suite of test from a default one and its 
alternatives.

Example (Junit 5):
```java
@TestFactory
Iterable<DynamicNode> testFormatName() {
  return TestTemplate
    // Default Test
    .defaultTest("should format the name")
    .given("first-name").is("Alice")
    .given("last-name").is("Brown")
    .when(ctx -> formatName(ctx.get("first-name"), ctx.get("last-name")))
    .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown, Alice"))
  
    // A first alternative test
    .test("should return only last name when first name is null")
    .sameAsDefault()
    .except("first-name").isNull()
    .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown"))
    
    // Another alternative test
    .test("should throw an exception when last name is null")
    .sameAsDefault()
    .except("last-name").isNull()
    .then(ctx -> assertThat(ctx.exception()).isInstanceOf(IllegalArgumentException.class))
    
    // Build the suite
    .suite();
}
```