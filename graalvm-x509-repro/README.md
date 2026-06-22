# GraalVM native-image: `X509CertInfo` analysis failure with a signed jar (JGit)

Minimal reproduction for a GraalVM `native-image` static-analysis failure.
Building a native image of an application that uses **Eclipse JGit** (which is
distributed as a **signed** jar) fails because JGit's jar-signing X509
certificate chain ends up in the image heap, and the analysis crashes with:

```
Error: Type not found during analysis:
  AnalysisType<X509CertInfo -> HotSpotType<Lsun/security/x509/X509CertInfo;, resolved>,
  instantiated: false, reachable: false>
  reading field sun.security.x509.X509CertImpl.info of constant
    sun.security.x509.X509CertImpl@...: [[ Version: V3
      Subject: CN="Eclipse.org Foundation, Inc.", ... ]]
  ...
  reading field com.oracle.svm.core.code.ImageCodeInfo.objectConstants ...
  scanning root com.oracle.svm.core.code.ImageCodeInfo[]@... embedded in
    com.oracle.svm.core.layeredimagesingleton.MultiLayeredImageSingleton.getAllLayers
```

The three certificates that get pulled in are JGit's code-signing chain:

* `CN="Eclipse.org Foundation, Inc."` (the jar signer)
* `CN=DigiCert Trusted G4 Code Signing RSA4096 SHA384 2021 CA1` (intermediate)
* `CN=DigiCert Trusted Root G4` (root)

## Environment

| | |
|---|---|
| GraalVM        | `native-image 25.0.2 2026-01-20` (GraalVM CE 25.0.2+10.1) |
| native-maven-plugin | `1.1.1` |
| Eclipse JGit   | `7.7.0.202606012155-r` |
| OS             | Linux x86_64 |

## How to run

```sh
./mvnw package
```

(or `mvn package` with a GraalVM 25 `JAVA_HOME`). The native image is built
unconditionally by the `native-maven-plugin` bound to the `package` phase; the
build fails during `[7/8] Building image...` while writing the image heap.

## What the program does

`repro.Main` just parses a Git object id:

```java
ObjectId id = ObjectId.fromString("0123456789012345678901234567890123456789");
System.out.println(id.name());
```

That is enough to make JGit's hashing code (`org.eclipse.jgit.util.sha1.SHA1`,
which goes through `java.security.MessageDigest`) reachable. Because JGit's jar
is signed, its `CodeSource` certificate chain becomes a constant referenced
from compiled code (`ImageCodeInfo.objectConstants`) and is scanned into the
image heap. The analysis then fails because `sun.security.x509.X509CertInfo`
(the type of `X509CertImpl.info`) was never marked as *instantiated*.

`src/main/resources/META-INF/native-image/native-image.properties` initializes
`org.eclipse.jgit.lib.ObjectId` at build time, mirroring the real-world project
where this was found, but the failure also reproduces without it.

## Expected vs. actual

* **Expected:** the native image builds. Signed-jar `CodeSource` certificates
  are build-time artifacts and should not be required to be present (and
  fully analyzable) in the image heap. They are never used at run time here.
* **Actual:** the build fails because the certificate object graph
  (`X509CertImpl` → `X509CertInfo` → `CertificateAlgorithmId`,
  `CertificateExtensions`, ...) is reachable in the heap but its types are not
  registered as instantiated by the static analysis.

## Notes / related

* Attempting to register the certificate types for reflection only moves the
  failure one field deeper each time (`X509CertInfo` → `CertificateAlgorithmId`
  → `CertificateExtensions` → ...), i.e. the whole `sun.security.x509` object
  graph would have to be registered, which is not a real fix.
* This first surfaced after `native-build-tools` was upgraded from `0.11.5`
  to `1.1.1` (e.g. via Spring Boot `4.0.x` → `4.1.0`), because `1.1.1` applies
  GraalVM reachability metadata for more dependencies, which is one of several
  ways to make the certificate-carrying code path reachable. Registering JGit's
  `org.eclipse.jgit.util.sha1.SHA1$Sha1Implementation` enum through the modern
  `reachability-metadata.json` format is another trigger.
