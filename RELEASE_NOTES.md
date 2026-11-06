# css4j version 6.2 Release Notes

### November 6, 2026

This is mainly a bugfix release, with 6.2.1 being a drop-in replacement for 6.2.
All users are encouraged to upgrade.

<br/>

## Highlights

### License is now BSD 2-clause

The license of this software used to contain the following point:

´´´
3. Neither the name of the copyright owner nor the names of
   contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.
´´´

It had the potential of alienating users that just wanted to give credit about
the software components that they use, so it was removed. Now the SPDX license
identifier is `BSD-2-Clause OR BSD-3-Clause`.

<br/>

## Detail of changes

- Switch to a 2-clause BSD license.
- nsac: do not allow 0 as 0% in keyframe selectors.
- cssom: fix a bug minifying the `@supports` rule with the `selector()` function.
- cssom: fix a bug serializing keyframe selectors in `@keyframe` rules.
- cssom: support `hanging` and `each-line` in `text-indent` property.
- cssom: support properties from Animation Triggers specification draft.
- cssom: set "display: block" for the `main` element in the UA sheet for HTML.
- cssom: add `unicode-bidi: isolate` for `menu` in the UA style sheet for HTML.
- Gradle: use `mavenCentral()` instead of explicit server.
- README, RELEASE_HOWTO: Java 25 is supported.
- RELEASE_HOWTO: Maven repo index generation uses new script.
- Bump year to 2026 in copyright notices.
- CI: build with Java 25.
- Upgrade to JUnit 5.14.4.
- Upgrade Gradle wrapper to 8.14.5.
- Upgrade to actions/checkout v7.
- Upgrade to actions/setup-java v6.
- Upgrade to actions/upload-artifact v7.
- Upgrade to gradle/actions v6.
