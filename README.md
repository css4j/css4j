# css4j

This project provides implementations of APIs similar to W3C/WHATWG's:

- [Document Object Model (DOM)](https://dom.spec.whatwg.org/).
- [CSS Object Model (CSSOM)](https://www.w3.org/TR/cssom-1/).

If you want to build css4j from the code that is currently at the Git repositories, you need a JDK version 12 and a copy of the `css4j-dist` repository first, then run the `tree.sh` script (which requires `subversion`). For example:
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
After running the script you'll have a tree with all the modules, ready to build with Maven (you can just run `mvn` but remember to set `JAVA_HOME` to JDK12 or higher first, if that is not your default).

Note that one of the dependencies ([JCLF](https://sourceforge.net/projects/jclf/)) [is at MVNRepository](https://mvnrepository.com/artifact/io.sf.jclf/jclf) but not in Maven Central. Depending on your setup, you may need to install it manually first.
