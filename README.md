# css4j

This project provides implementations of APIs similar to W3C/WHATWG's:

- [Document Object Model (DOM)](https://dom.spec.whatwg.org/).
- [CSS Object Model (CSSOM)](https://www.w3.org/TR/cssom-1/).

Downloads of a ZIP archive containing all the artifacts are available from the [`css4j-dist` release area](https://github.com/css4j/css4j-dist/releases) and also from a [mirror site](https://sourceforge.net/projects/carte/files/css4j/).

To build css4j from the code that is currently at the Git repositories, you need a current JDK (the build is tested with version 15) and a copy of the `css4j-dist` repository first, then run the `tree.sh` script. For example:
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
After running the script you'll have a tree with all the modules, ready to build with Maven. You can just run `mvn` (which defaults to `clean package`) but remember to set `JAVA_HOME` to JDK 15 or higher first, if that is not your default.

Note that one of the dependencies ([JCLF](https://sourceforge.net/projects/jclf/)) is not in Maven Central. You probably need to install it manually first, which can be done easily with the [`install-jclf.sh`](https://raw.githubusercontent.com/css4j/css4j-dist/master/install-jclf.sh) script.
You can also directly install the CSS4J artifacts into your local Maven repository, with the similar [`install-css4j.sh`](https://raw.githubusercontent.com/css4j/css4j-dist/master/install-css4j.sh).

For more information: https://css4j.github.io/
