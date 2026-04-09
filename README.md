# TestTemplate

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/testtemplate/testtemplate?sort=semver)](https://github.com/testtemplate/testtemplate/releases)
[![Java 21](https://img.shields.io/badge/Java-21-blue)](#)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-green)](#)
[![License](https://img.shields.io/github/license/testtemplate/testtemplate)](https://github.com/testtemplate/testtemplate/blob/master/LICENSE)

TestTemplate is a Java testing framework that lets you write parameterized tests as **reusable templates**.
Define your test scenario once with named variables; then declare alternative test cases that inherit the
same template and override only the variables that differ. JUnit 5's dynamic test engine renders each
variant as its own test node in the test tree.

Example (Junit 5):
```java
@TestFactory
Iterable<DynamicNode> testFormatName() {
  return TestTemplate
    // Default Test
    .defaultTest("should format the name")
    .given("first-name").is("Alice")
    .given("middle-name").is("Julia")
    .given("last-name").is("Brown")
    .when(ctx -> formatName(ctx.get("first-name"), ctx.get("middle-name"), ctx.get("last-name")))
    .then(ctx -> assertThat(ctx.result()).isEqualTo("Brown, Alice J"))
  
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

## License

Apache 2.0 — see [LICENSE](LICENSE).
