# Contributing to CSS4J

 You can contribute to the CSS4J project by raising issues and/or sending `git`
pull requests.

<br/>

## Report issues

 If you find any issue with the software or want to ask for an enhancement, use
the Github's [issue tracker](https://github.com/css4j/css4j/issues).

 Note that some issues may correspond to other subprojects, for example
[xml-dtd issues](https://github.com/css4j/xml-dtd/issues).

<br/>

## Pull requests

 To contribute code to this project it is recommended to open an issue first,
explaining the rationale for the changes that you want to implement. Then, in
the title of the pull request (PR) you can include a reference like "fixes #NN"
where NN is the issue number. And it is generally a good idea to base your PR on
a branch that was named after the issue; for example your branch could be named
`issue-4`.

 A PR should only try to fix a single issue, unless it fixes two or more issues
that are very related or effectively the same. And if a commit has two or more
different purposes, it is often better to split it in multiple commits; tools
like the _Git GUI_ are particularly useful for that.

 It is recommended that commit messages (or at least the message for the main
commit) start with a prefix related to the area being affected. For example:
```
CSSOM: support lab() and lch() color functions.
```
 The next prefixes are commonly used:
- NSAC: NSAC API and implementation (lexical-level parser).
- DOM: native DOM implementation (`doc.dom`) and any DOM stuff in `doc` package.
- DOM wrapper: anything related to `StylableDocumentWrapper`.
- CSSOM: CSS Object Model.

 If the commit has a wider scope than a single area you do not need to include
any prefix, for example:
```
Add .gitattributes file.
```
 When the commit is exclusively about tests, you could use the "Tests:" prefix.
Same for "Javadoc:".

<br/>

### Tests

 All PRs should come with one or more JUnit tests unless the change is a small,
obviously correct fix. Ideally, tests should provide a full coverage of the new
code, except for cases like multiple equivalent comparisons, exceptions that are
very hard to trigger or never thrown (or put as a theoretical safeguard), etc.
However, even one test is better than nothing.

 For consistency, the names of classes that contain tests (i.e. with `@Test`
annotation(s)) must end with `Test`. This makes easier to tell the actual tests
apart from their helper classes.

 It is acceptable to mix tests and main source code in the same commit, as it is
immediately obvious to the reviewers which are which, and the tests offer an
insight of what the commit is trying to achieve. You do not need to mention the
tests in the commit message.

<br/>

### Benchmarks

 CSS4J uses [JMH](https://github.com/openjdk/jmh) for its [benchmarks](https://github.com/css4j/benchmark).
The names of all the classes containing benchmarks must end with `Mark` for ease
of identification (a similar convention to tests ending with `Test`).

<br/>

### Code style

 The code style could be summarized by the following points:

- Indent by tabs, not spaces. The automated formatting provided by the Eclipse
IDE is often used.
- `if`-`else` blocks should always use curly braces, even if a single line of
code is involved.
- Long, descriptive variable names are preferred.
- Add comments to explain what the code is trying to do, but avoiding useless
prose that just mimics the code, like _"check if foo is larger than 1"_ as a
comment to `if (foo > 1)`.
- Public and protected methods must have documentation comments.
- Avoid trailing whitespace except for empty lines in Javadoc comments.
- Classes and methods should have the minimum visibility that they require.
A method should not have `protected` visibility when being package-visible could
be enough, unless subclasses in other packages would naturally extend it. For
complex package-level or inner classes, it is acceptable to have `protected`
methods as a mean to document which ones are intended to be overridden by other
classes. (In that case, protected methods do not appear in the Javadocs and
therefore are not part of the API)

<br/>

### Copyright and attribution

 All contributions are submitted under a [Developer Certificate of Origin](DeveloperCertificateOfOrigin.txt).
Although every author maintains their copyright, in case that code from this
project —including your contribution(s)— is used in a way that is deemed as a
license infringement, you are —in principle— not opposed to be represented by
the maintainer of this project in any legal proceedings intended to protect the
license of this project.

 If you modify a source file that happens to have an `@author` tag with the
author name, feel free to remove that field and let `git blame` handle the
attribution.

 It is assumed that if you put an `@author` field with your name in a new file,
the same criteria would apply.

<br/>

### Web site

 The websites of open source projects can always be improved; if you have a
contribution to this project's web site, [a PR would be welcome](https://github.com/css4j/css4j.github.io/pulls).

<br/>

## Licensing

 Your contributions are to be submitted according to the license of this
project, see the LICENSE.txt file for more information. There is an exception
though: as a courtesy to W3C, interface definitions that are based on W3C APIs
are published under a W3C license and its copyright, see the LICENSES.txt file.
Note, however, that there are differing interpretations as to whether APIs can
be copyrighted.

<br/>

## Distribution

 This project [is not being submitted to the Maven Central repository](https://groups.google.com/g/css4j/c/op5jIoINb3M/m/IiiN-LfkDAAJ)
and this is something known to deter some contributors, thus being a relevant
information to cover here.

 However, this project distributes its artifacts through the css4j Maven
repository, as explained in the [README](README.md) (see 'Usage from a Gradle
project'). Please use that repository **only** for the artifact groups that it
supplies.
