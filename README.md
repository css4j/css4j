# css4j

This project provides implementations of APIs similar to W3C/WHATWG's:

- [Document Object Model (DOM)](https://dom.spec.whatwg.org/).
- [CSS Object Model (CSSOM)](https://www.w3.org/TR/cssom-1/).

Downloads of a ZIP archive containing all the artifacts are available from the [`css4j-dist` release area](https://github.com/css4j/css4j-dist/releases) and also from a [mirror site](https://sourceforge.net/projects/carte/files/css4j/).

<br/>

## Build from source
To build css4j from the code that is currently at the Git repositories, you need a current JDK (the build is tested with
version 16) and a copy of the `css4j-dist` repository (**not** this repository), then run the `tree.sh` script
(which will fetch this repository together with several others). For example:
```
    git clone https://github.com/css4j/css4j-dist.git css4j-snapshot
    cd css4j-snapshot
    ./tree.sh
```
Or download a zip and extract:
```
    /usr/bin/curl -o css4j-dist-master.zip https://codeload.github.com/css4j/css4j-dist/zip/master
    /usr/bin/unzip css4j-dist-master.zip
    cd css4j-dist-master
    ./tree.sh
```
After running the script you'll have a tree with all the modules, ready to build with Gradle (version 7.0 or higher is
required). You can run a variety of Gradle tasks:

- `gradle build` (normal build)
- `gradle build publishToMavenLocal` (to install in local Maven repository)
- `gradle copyJars` (to copy jar files into a top-level _jar_ directory)
- `gradle lineEndingConversion` (to convert line endings of top-level text files to CRLF)
- `gradle publish` (to deploy to a Maven repository, as described in the `publishing.repositories.maven` block of
[css4j.java-conventions.gradle](https://github.com/css4j/css4j-dist/blob/master/buildSrc/src/main/groovy/css4j.java-conventions.gradle))

If you do not have Gradle installed, it is easy to do so using a package manager (for example [`scoop`](https://scoop.sh/)
in Windows or [SDKMAN!](https://sdkman.io/) on Linux).

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
            includeGroup 'xmlpull'
            includeGroup 'xpp3'
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
If you build your project (that depends on css4j) with Maven, please note that some of the css4j dependencies are not in
Maven Central:
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
<dependency>
    <groupId>io.sf.carte</groupId>
    <artifactId>css4j</artifactId>
    <version>${css4j.version}</version>
</dependency>
```

<br/>

## Website
For more information please see https://css4j.github.io/
