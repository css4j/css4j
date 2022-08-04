# How to produce a css4j release

Please follow these steps to produce a new release of css4j.


## Requirements

- The [Git version control system](https://git-scm.com/downloads) is required to
obtain the sources. Any recent version should suffice.

- Java 11 or later. You can install it from your favourite package manager or by
downloading from [Adoptium](https://adoptium.net/).

- The [`generate_directory_index_caddystyle.py`](https://gist.github.com/carlosame/bd5b68c4eb8e0817d9beb1dcfb4de43d)
script and a recent version of [Python](https://www.python.org/) (required to
run it). The script is necessary to create the index files in the bare-bones
Maven repository currently used by css4j.

- The `sha1sum`, `sha256sum`, `b2sum` and `md5sum` Unix-compatible hash
utilities in the `/usr/bin` directory. Can be installed on Windows as well,
either via [MSYS2](https://www.msys2.org/) or by [manually installing over the
Git Bash](https://gist.github.com/carlosame/5c4070c3941707c0f2c2a5bf1b175cc4).


## Steps

1) Obtain a fresh local copy of the css4j Git repository with:
```shell
git clone git@github.com:css4j/css4j.git
```

For reference, let your copy of the css4j release code be at `/path/to/css4j`.

2) In the `master` branch of your local copy of the css4j Git repository, bump
the `version` in the [`build.gradle`](build.gradle) file or remove the
`-SNAPSHOT` suffix as necessary. Commit the change to the Git repository.

3) If there is an issue tracking the release, close it (could be done adding a
'closes...' to the message in the previously described commit).

4) To check that everything is fine, build the code:

```shell
cd /path/to/css4j
./gradlew build
```

5) Now copy the produced _jar_ files into a new `jar` directory:

```shell
./gradlew copyJars
```

Manually copy there the current compatible _jar_ files for the `css4j-agent`,
`css4j-awt` and `css4j-dom4j` modules.

6) Use `changes.sh <new-version>` to create a `CHANGES.txt` file with the
changes from the latest tag. For example if you are releasing `3.9.1`:

```shell
./changes.sh 3.9.1
```

Edit the resulting `CHANGES.txt` as convenient, to use it as the basis for the
detailed list of changes when you create the new release in Github.

7) Clone the `git@github.com:css4j/css4j.github.io.git` repository (which
contains a bare-bones Maven repository) and let `/path/to/css4j.github.io` be
its location.

8) From your copy of the css4j release code, write the new artifacts into the
local copy of the bare-bones Maven repository with:

```shell
cd /path/to/css4j
./gradlew publish -PmavenReleaseRepoUrl="file:///path/to/css4j.github.io/maven"
```

9) Produce the necessary directory indexes in the local copy of the bare-bones
Maven repository using [`generate_directory_index_caddystyle.py`](https://gist.github.com/carlosame/bd5b68c4eb8e0817d9beb1dcfb4de43d):

```shell
cd /path/to/css4j.github.io/maven/io/sf/carte
generate_directory_index_caddystyle.py -r css4j
```

10) Create a `v<version>` tag in the css4j Git repository. For example:

```shell
cd /path/to/css4j
git tag -s v3.9.1 -m "Milestone Release 3.9.1"
git push origin v3.9.1
```

or `git tag -a` instead of `-s` if you do not plan to sign the tag. But it is
generally a good idea to sign a release tag.

Alternatively, you could create the new tag when drafting the Github release
(step 14).

11) Now prepare the release Zip file by running `./gradlew clean`, removing the
unnecessary directories and finally creating the archive. In the following
example we create the 3.9.1 release Zip with the `7z` compressor:

```shell
cd /path/to/css4j
./gradlew clean
rm -fr .git .gradle buildSrc/.gradle
cd ..
mv css4j css4j-3.9.1
7z a css4j-3.9.1.zip css4j-3.9.1
```

12) Create the release's digest files:

```shell
cd /path/to/css4j.github.io/digest
./ha.sh /path/to/css4j-3.9.1.zip
```

You should now have the `css4j-3.9.1.sha1`, `css4j-3.9.1.sha256`,
`css4j-3.9.1.b2` and `css4j-3.9.1.md5` files under the `digest` directory.

13) Upload the newly created digest files and the Zip release to the appropriate
folders in https://sourceforge.net/projects/carte/files/css4j/

14) Draft a new Github release at https://github.com/css4j/css4j/releases

Summarize the most important changes in the release description, then create a
`## Detail of changes` section and paste the contents of the `CHANGES.txt` file
under it.

Add to the Github release the _jar_ files from the `jar` directory in your copy
of the css4j release.

15) Verify that the new [Github packages](https://github.com/orgs/css4j/packages?repo_name=css4j)
were created successfully by the [Gradle Package](https://github.com/css4j/css4j/actions/workflows/gradle-publish.yml)
task.

16) Edit the `index.html` file in the `css4j.github.io` repository to reflect
the new version number and the new download URLs, Release Notes included. If the
changes to that repo look correct, commit them.

17) Check whether the ["Examples" CI](https://github.com/css4j/css4j.github.io/actions/workflows/examples.yml)
triggered by the commit to the `css4j.github.io` repository (in the previous
step) completed successfully. A failure could mean that the artifacts are not
usable with Java 8, for example.

18) Clone the [css4j-dist](https://github.com/css4j/css4j-dist) repository, and
update the css4j version number in the
[install-css4j.sh](https://github.com/css4j/css4j-dist/blob/master/maven/install-css4j.sh)
script. Commit the change and look for the completion of that project's CI.