# Reporting a vulnerability

<br/>

Only the latest version is supported, and it is recommended that you check
whether the latest `master` branch is vulnerable before reporting any issue.

If you think that you have found an XXE vulnerability, please skip to next
section.

To report a security vulnerability, please read
[Privately reporting a security vulnerability](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing/privately-reporting-a-security-vulnerability#privately-reporting-a-security-vulnerability).

<br/>

## `XMLDocumentBuilder` and XXE

The `XMLDocumentBuilder.java` does not use the `load-external-dtd` nor
`disallow-doctype-decl` features, resorting to different strategies instead
to keep parsing safe (the aforementioned configurations cause data loss when
entities are used). All of the security scanners tested so far do not flag any
vulnerability on css4j. However it is possible that some future scanner behaves
otherwise, so this section was written.

See [XML parsing in Java with `DefaultEntityResolver`](https://css4j.github.io/resolver.html)
for details. In fact there are multiple unit tests that check how `XMLDocumentBuilder`
handles DTDs, with and without a secure resolver.
