# css4j

This project provides implementations of a few W3C/WHATWG APIs:

- [Document Object Model (DOM)](https://dom.spec.whatwg.org/).
- [CSS Object Model (CSSOM)](https://www.w3.org/TR/cssom-1/).
- [SAC](https://www.w3.org/Style/CSS/SAC/) (in an updated variant called NSAC).

If you want to build css4j from the code that is currently at the Git repositories, you need a JDK version 12 and a copy of the `css4j-dist` repository first, then run the `tree.sh` script. For example:
```
    git clone https://github.com/css4j/css4j.git css4j-snapshot
    cd css4j-snapshot
    ./tree.sh
```
After running the script you'll have a tree with all the modules, ready to build with Maven (just run `mvn`). Note that one of the dependencies (JCLF) is at MVNRepository but not in Maven Central. Depending on your setup, you may need to install it manually first.

