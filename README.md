![CI workflow](https://github.com/css4j/css4j/actions/workflows/build.yml/badge.svg)

# css4j

This project provides implementations of APIs similar to W3C/WHATWG's:

- [Document Object Model (DOM)](https://dom.spec.whatwg.org/).
- [CSS Object Model (CSSOM)](https://www.w3.org/TR/cssom-1/).

Unless otherwise noted, this software is provided under a [BSD-style license](LICENSE.txt)
(see also [NOTICE.txt](NOTICE.txt) for included files that have a different licensing).

<br/>

## Features

The functionality of this library can be divided in the following areas:

- A CSSOM API very similar to the standard W3C [CSS Object Model API](https://www.w3.org/TR/cssom-1/),
  that allows accessing the CSS information (style sheets, inline styles, etc.)
  in a DOM Document, as well as getting computed styles for its elements.

- A CSS-enabled [native DOM implementation](https://css4j.github.io/api/latest/io.sf.carte.css4j/io/sf/carte/doc/dom/package-summary.html).

- A DOM wrapper that can be used with an external DOM implementation.

- A device profile API to encapsulate target device-specific information.

In separate modules you can find:

- A DOM4J backend (module [css4j-dom4j](https://github.com/css4j/css4j-dom4j))
  which uses [documents and elements that extend those of dom4j](https://css4j.github.io/api/latest/io.sf.carte.css4j.dom4j/module-summary.html)
  (for those who are comfortable with the [dom4j API](https://dom4j.github.io/)).

- [User agent helper classes](https://css4j.github.io/api/latest/io.sf.carte.css4j.agent.net/module-summary.html)
  that ease the usage of the library with correct cookie settings etc.
  ([css4j-agent](https://github.com/css4j/css4j-agent) module).

- A few [AWT helper classes](https://css4j.github.io/api/latest/io.sf.carte.css4j.awt/module-summary.html)
  ([AWT module](https://github.com/css4j/css4j-awt)).

<br/>

## Java™ Runtime Environment requirements
All the classes in the binary package have been compiled with a [Java compiler](https://adoptium.net/)
set to 1.8 compiler compliance level, except the `module-info.java` file.

Building the library requires JDK 11 or higher.

<br/>

## Build from source
To build css4j from the code that is currently at the Git repository, JDK 11 or later is needed.
You can run a variety of Gradle tasks with the Gradle wrapper (on Windows shells you can omit the `./`):

- `./gradlew build` (normal build)
- `./gradlew publishToMavenLocal` (to install in local Maven repository)
- `./gradlew copyJars` (to copy jar files into a top-level _jar_ directory)
- `./gradlew testOn8` (run tests with Java 8)
- `./gradlew jacocoTestReport` (produces a test coverage report in the `build/reports/jacoco/test/html` directory)
- `./gradlew publish` (deploys to a Maven repository, as described in the `publishing.repositories.maven` block of
[build.gradle](https://github.com/css4j/css4j/blob/master/build.gradle))

<br/>

## Usage from a Gradle project
If your Gradle project depends on css4j, you can use this project's own Maven repository in a `repositories` section of
your build file:
```groovy
repositories {
    maven {
        url "https://css4j.github.io/maven/"
        mavenContent {
            releasesOnly()
        }
        content {
            // Include all the groups used by popular io.sf.* projects
            includeGroupByRegex 'io\\.sf\\..*'

            // Alternatively:
            //includeGroup 'io.sf.carte'
            //includeGroup 'io.sf.jclf'
        }
    }
}
```
please use this repository **only** for the artifact groups listed in the `includeGroup` statements.

Then, in your `build.gradle` file:
```groovy
dependencies {
    api "io.sf.carte:css4j:${css4jVersion}"
}
```
where `css4jVersion` would be defined in a `gradle.properties` file.

<br/>

## Usage from a Maven build
If you build your project (that depends on css4j) with Maven, please note that
neither css4j nor some of its dependencies are in Maven Central.

The easiest path is to add the css4j Maven repository to your project's POM:

```xml
<repositories>
    <repository>
        <id>css4j</id>
        <name>CSS4J repository</name>
        <url>https://css4j.github.io/maven/</url>
    </repository>
</repositories>
```

And then, add the following to the `<dependencies>` section of your `pom.xml`:

```xml
<!-- This artifact is not in Maven Central -->
<dependency>
    <groupId>io.sf.carte</groupId>
    <artifactId>css4j</artifactId>
    <version>${css4j.version}</version>
</dependency>
```

<br/>

## In your IDE

When running the test suite from your IDE, you may want to exclude the `Fuzz`
tag from your JUnit Runner configuration, to avoid bootstrapping the fuzzer each
time you run it.

<br/>

## Fuzzing

This library includes a [Jazzer](https://github.com/CodeIntelligenceTesting/jazzer)-based
fuzzing test that can be run with

```shell
./gradlew fuzzer
```

as well as within your IDE (in the latter case you have to set the `JAZZER_FUZZ`
environment variable).

Beware that many of the crashes that it finds are related to the coverage
instrumentation and cannot be reproduced outside of the fuzzer.

<br/>

## Software dependencies

In case that you do not use a Gradle or Maven build (which would manage the
dependencies according to the relevant `.module` or `.pom` files), the required
and optional library packages are the following:

### Compile-time dependencies

- The [jclf-text](https://jclf.sourceforge.io/api/io.sf.jclf.text/module-summary.html)
  (5.0.0 or higher) and [jclf-linear3](https://jclf.sourceforge.io/api/io.sf.jclf.math.linear3/module-summary.html)
  (1.0.0 or higher) modules. See: https://sourceforge.net/projects/jclf

- The [carte-util](https://github.com/css4j/carte-util) library; version 3.6.0
  or higher.

- The [tokenproducer](https://github.com/css4j/tokenproducer) library; version
  3.0 or higher is required.

- The [xml-dtd](https://github.com/css4j/xml-dtd) library; version 4.2.1 or
  higher is required.
  **It is optional at runtime.**

- [SLF4J](http://www.slf4j.org/), which is a logging package.
  **It is optional at runtime.**

### Test dependencies

- A recent version of [JUnit 5](https://junit.org/junit5/) with the
  `junit-vintage-engine` artifact.

- The [validator.nu html5 parser](https://about.validator.nu/htmlparser/).

<br/>

## Website

For more information please see https://css4j.github.io/
