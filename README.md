![CI workflow](https://github.com/css4j/css4j/actions/workflows/build.yml/badge.svg)

# css4j

This project provides implementations of APIs similar to W3C/WHATWG's:

- [Document Object Model (DOM)](https://dom.spec.whatwg.org/).
- [CSS Object Model (CSSOM)](https://www.w3.org/TR/cssom-1/).

Unless otherwise noted, this software is provided under a [BSD-style license](LICENSE.txt)
(see also [LICENSES.txt](LICENSES.txt) for included files that have a different licensing).

<br/>

## Features

The functionality of this library can be divided in the following areas:

- A CSSOM API very similar to the standard W3C [CSS Object Model API](https://www.w3.org/TR/cssom-1/),
  that allows accessing the CSS information (style sheets, inline styles, etc.)
  in a DOM Document, as well as getting computed styles for its elements.

- A CSS-enabled [native DOM implementation](https://css4j.github.io/api/3/io/sf/carte/doc/dom/package-summary.html).

- A DOM wrapper that can be used with an external DOM implementation.

- A device profile API to encapsulate target device-specific information.

In separate modules you can find:

- A DOM4J backend (module [css4j-dom4j](https://github.com/css4j/css4j-dom4j))
  which uses documents and elements that extend those of dom4j (for those who
  are comfortable with the [dom4j API](https://dom4j.github.io/)).

- User agent helper classes, that ease the usage of the library with correct
  cookie settings etc. ([css4j-agent](https://github.com/css4j/css4j-agent) module).

- A few AWT helper classes ([AWT module](https://github.com/css4j/css4j-awt)).

<br/>

## CSS3 Support

CSS3 is partially supported. The following table summarizes the basic support
for setting/retrieving the main CSS level 3/4 features (other specifications are
also supported):

 | CSS Spec Name | Support |
 |---|---|
 | [Background / Border](https://www.w3.org/TR/css-backgrounds-3/) | Yes |
 | [Color](https://www.w3.org/TR/css-color-4/) | Partial (1) |
 | [Media Queries](https://www.w3.org/TR/mediaqueries-4/) | Partial (2) |
 | [Selectors](https://www.w3.org/TR/selectors-4/) | Yes |
 | [Transitions](https://www.w3.org/TR/css-transitions-1/) | Yes |
 | [Values](https://www.w3.org/TR/css-values-4/) | Yes |
 | [Properties and Values API](https://www.w3.org/TR/css-properties-values-api-1/) | Yes |
 | [Grid / Template / Alignment](https://www.w3.org/TR/css-grid-2/) | Partial (3) |

Notes:
 1) Level 3 is supported, level 4 partially.
 2) Event handling with `addListener`/`removeListener` is not supported, given that
the library's user is supposed to be in control of the `CSSCanvas` instances where
the information about such events should be available.
 3) Legacy gap properties (`grid-row-gap`, `grid-column-gap`, and `grid-gap`) are not
supported, although the longhands can be used if declared explicitly).

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
- `./gradlew build publishToMavenLocal` (to install in local Maven repository)
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
            includeGroup 'io.sf.carte'
            includeGroup 'io.sf.jclf'
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
some of the css4j dependencies are not in Maven Central:

- [JCLF](https://sourceforge.net/projects/jclf/).

- [XMLPull-XPP3](https://github.com/xmlpull-xpp3/xmlpull-xpp3) (v1.2, dependency of the DOM4J module only).

You may want to install them manually into your local Maven repository, which can be done easily with the:

- [`install-jclf.sh`](https://raw.githubusercontent.com/css4j/css4j-dist/master/maven/install-jclf.sh)
- [`install-xpp3.sh`](https://raw.githubusercontent.com/css4j/css4j-dist/master/maven/install-xpp3.sh)

scripts.

You can also directly install the CSS4J artifacts into your local Maven repository, with the similar
[`install-css4j.sh`](https://raw.githubusercontent.com/css4j/css4j-dist/master/maven/install-css4j.sh).

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
  1.1.1 or higher is recommended.

- The [xml-dtd](https://github.com/css4j/xml-dtd) library; version 4.0.0 or
  higher is recommended.
  **It is optional at runtime.**

- [SLF4J](http://www.slf4j.org/), which is a logging package.
  **It is optional at runtime.**

### Test dependencies

- A recent version of [JUnit 4](https://junit.org/junit4/).

- The [validator.nu html5 parser](https://about.validator.nu/htmlparser/).

<br/>

## Website

For more information please see https://css4j.github.io/
